package io.pivotal.cfapp.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.convert.CustomConversions.StoreConversions;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class R2dbcConfig {

	private static final String DOMAIN_PACKAGE = "io.pivotal.cfapp.domain";
	
	@Bean
	public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory factory	) {
		return new R2dbcCustomConversions(getStoreConversions(factory), getCustomConverters());
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
	            log.info("Added an instance of " + beanDef.getBeanClassName() + "to list of customer converters.");
	        } catch (Exception e) {
	            log.error("Could not add an instance of "+ beanDef.getBeanClassName() + " to list of custom converters.", e);
	        }
        }
	    return converterList;
	}

	protected StoreConversions getStoreConversions(ConnectionFactory factory) {

		R2dbcDialect dialect = getDialect(factory);

		List<Object> converters = new ArrayList<>(dialect.getConverters());
		converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS);

		return StoreConversions.of(dialect.getSimpleTypeHolder(), converters);
	}
	
	protected R2dbcDialect getDialect(ConnectionFactory connectionFactory) {
		return DialectResolver.getDialect(connectionFactory);
	}
}
