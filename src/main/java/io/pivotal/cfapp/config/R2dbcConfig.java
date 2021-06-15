package io.pivotal.cfapp.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.util.StringUtils;

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
class R2dbcConfig extends AbstractR2dbcConfiguration {
    
    private static final String DOMAIN_PACKAGE = "io.pivotal.cfapp.domain";
    private static final List<String> SUPPORTED_SCHEMES = Arrays.asList(new String[] { "mysql", "postgresql"});
    private static final String VCAP_SERVICE = "cf-butler-backend";
    
    private R2dbcProperties r2dbcProperties;

    public R2dbcConfig(R2dbcProperties r2dbcProperties) {
        super();
        this.r2dbcProperties = r2dbcProperties;
    }

    @Bean
    @Profile("cloud")
    public CfEnv cfEnv() {
        return new CfEnv();
    }

    @Bean
    @Profile("cloud")
    public ConnectionFactory connectionFactory() {
        R2dbcProperties properties = r2dbcProperties(cfEnv());
        ConnectionFactoryOptions.Builder builder = ConnectionFactoryOptions
                .parse(properties.getUrl()).mutate();
        String username = properties.getUsername();
        if (StringUtils.hasText(username)) {
            builder.option(ConnectionFactoryOptions.USER, username);
        }
        String password = properties.getPassword();
        if (StringUtils.hasText(password)) {
            builder.option(ConnectionFactoryOptions.PASSWORD, password);
        }
        String databaseName = properties.getName();
        if (StringUtils.hasText(databaseName)) {
            builder.option(ConnectionFactoryOptions.DATABASE, databaseName);
        }
        if (properties.getProperties() != null) {
            properties.getProperties()
            .forEach((key, value) -> builder
                    .option(Option.valueOf(key), value));
        }
        return ConnectionFactories.get(builder.build());
    }

    // support for external R2DBC source is limited to providers that support URI scheme
    private R2dbcProperties r2dbcProperties(CfEnv cfenv) {
    	try {
            CfService service = cfenv.findServiceByName(VCAP_SERVICE);
            URI uri = service.getCredentials().getUriInfo().getUri();
            String scheme = uri.getScheme();
            log.info("Attempting to connect to a {} database instance.", scheme);
            if (scheme.startsWith("postgres")) {
                scheme = "postgresql";
            }
            if (SUPPORTED_SCHEMES.contains(scheme)) {
                R2dbcProperties r2dbcProperties = new R2dbcProperties();
                r2dbcProperties.setName(uri.getPath().replaceAll("/",""));
                String[] userInfoParts = uri.getUserInfo().split(":");
                String username = userInfoParts[0];
                String password = userInfoParts[1];
                r2dbcProperties.setUsername(username);
                r2dbcProperties.setPassword(password);
                StringBuilder builder = new StringBuilder();
                builder.append(String.format("r2dbc:pool:%s://", scheme));
                builder.append(uri.getHost());
                if (uri.getPort() != -1) {
                    builder.append(":" + uri.getPort());
                }
                builder.append(uri.getPath());
                r2dbcProperties.setUrl(builder.toString());
                return r2dbcProperties;
            } else {
                throw new IllegalStateException(String.format("Could not initialize R2dbcProperties. Service instance was found with scheme {} but it is not supported. Supported schemes are {}.", uri.getScheme(), SUPPORTED_SCHEMES));
            }
        } catch (IllegalArgumentException iae) {
            log.info("No bound service instance named {} was found. Falling back to embedded database.", VCAP_SERVICE);
            return this.r2dbcProperties;
        }
    }

    protected List<Object> getCustomConverters() {
        List<Object> converterList = new ArrayList<>();
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(ReadingConverter.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(WritingConverter.class));
        for (BeanDefinition beanDef : provider.findCandidateComponents(DOMAIN_PACKAGE)) {
            try {
                Class<?> cl = Class.forName(beanDef.getBeanClassName());
                converterList.add(cl.getDeclaredConstructor().newInstance());
                log.info("Added an instance of " + beanDef.getBeanClassName() + " to list of custom converters.");
            } catch (Exception e) {
                log.error("Could not add an instance of "+ beanDef.getBeanClassName() + " to list of custom converters.", e);
            }
        }
        return converterList;
    }

}
