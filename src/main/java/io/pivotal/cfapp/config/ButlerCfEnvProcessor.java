package io.pivotal.cfapp.config;

import java.util.Map;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

public class ButlerCfEnvProcessor implements CfEnvProcessor {

    private static final String SERVICE_NAME = "cf-butler-secrets";

    private static void addOrUpdatePropertyValue(String propertyName, String credentialName, CfCredentials cfCredentials, Map<String, Object> properties) {
        Object credential = cfCredentials.getMap().get(credentialName);
        if (credential != null) {
            properties.put(propertyName, credential);
        }
    }

    private static void addPropertyValue(String propertyName, Object propertyValue, Map<String, Object> properties) {
        properties.put(propertyName, propertyValue);
    }

    @Override
    public boolean accept(CfService service) {
        return
                service.getName().equalsIgnoreCase(SERVICE_NAME);
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return
                CfEnvProcessorProperties
                .builder()
                .serviceName(SERVICE_NAME)
                .build();
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        addPropertyValue("credhub.url", "https://credhub.service.cf.internal:8844", properties);
        addOrUpdatePropertyValue("spring.mail.host", "MAIL_HOST", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.mail.port", "MAIL_PORT", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.mail.username", "MAIL_USERNAME", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.mail.password", "MAIL_PASSWORD", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.mail.properties.mail.smtp.auth", "MAIL_SMTP_AUTH_ENABLED", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.mail.properties.mail.smtp.starttls.enable", "MAIL_SMTP_STARTTLS_ENABLED", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.sendgrid.api-key", "SENDGRID_API-KEY", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.r2dbc.url", "R2DBC_URL", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.r2dbc.username", "R2DBC_USERNAME", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.r2dbc.password", "R2DBC_PASSWORD", cfCredentials, properties);
        addOrUpdatePropertyValue("notification.engine", "NOTIFICATION_ENGINE", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.apiHost", "CF_API-HOST", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.username", "CF_USERNAME", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.password", "CF_PASSWORD", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.sslValidationSkipped", "CF_SKIP-SSL-VALIDATION", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.connectionPoolSize", "CF_CONNECTION_POOLSIZE", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.connectionTimeout", "CF_CONNECTION_TIMEOUT", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.tokenProvider", "CF_TOKEN-PROVIDER", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.refreshToken", "CF_REFRESH-TOKEN", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.organizationBlackList", "CF_ORGANIZATION-BLACK-LIST", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.spaceBlackList", "CF_SPACE-BLACK-LIST", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.accountRegex", "CF_ACCOUNT-REGEX", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.policies.git.uri", "CF_POLICIES_GIT_URI", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.policies.git.username", "CF_POLICIES_GIT_USERNAME", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.policies.git.password", "CF_POLICIES_GIT_PASSWORD", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.policies.git.commit", "CF_POLICIES_GIT_COMMIT", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.policies.git.filePaths", "CF_POLICIES_GIT_FILE-PATHS", cfCredentials, properties);
        addOrUpdatePropertyValue("cf.buildpacks", "CF_BUILDPACKS", cfCredentials, properties);
        addOrUpdatePropertyValue("om.apiHost", "OM_API-HOST", cfCredentials, properties);
        addOrUpdatePropertyValue("om.clientId", "OM_CLIENT-ID", cfCredentials, properties);
        addOrUpdatePropertyValue("om.clientSecret", "OM_CLIENT-SECRET", cfCredentials, properties);
        addOrUpdatePropertyValue("om.username", "OM_USERNAME", cfCredentials, properties);
        addOrUpdatePropertyValue("om.password", "OM_PASSWORD", cfCredentials, properties);
        addOrUpdatePropertyValue("om.enabled", "OM_ENABLED", cfCredentials, properties);
        addOrUpdatePropertyValue("om.grantType", "OM_GRANT-TYPE", cfCredentials, properties);
        addOrUpdatePropertyValue("pivnet.apiToken", "PIVNET_API-TOKEN", cfCredentials, properties);
        addOrUpdatePropertyValue("pivnet.enabled", "PIVNET_ENABLED", cfCredentials, properties);
        addOrUpdatePropertyValue("cron.collection", "CRON_COLLECTION", cfCredentials, properties);
        addOrUpdatePropertyValue("cron.execution", "CRON_EXECUTION", cfCredentials, properties);
        addOrUpdatePropertyValue("management.endpoints.web.exposure.include", "EXPOSED_ACTUATOR_ENDPOINTS", cfCredentials, properties);
    }
}
