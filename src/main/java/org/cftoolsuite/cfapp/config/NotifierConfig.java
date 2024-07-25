package org.cftoolsuite.cfapp.config;

import org.cftoolsuite.cfapp.notifier.EmailNotifier;
import org.cftoolsuite.cfapp.notifier.JavaMailNotifier;
import org.cftoolsuite.cfapp.notifier.SendGridNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.sendgrid.SendGridAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import com.sendgrid.SendGrid;

@Configuration
public class NotifierConfig {

    @Configuration
    @ConditionalOnProperty(prefix="notification", name="engine", havingValue="java-mail")
    @EnableAutoConfiguration(exclude = { SendGridAutoConfiguration.class })
    static class MailConfig {

        @Bean
        public EmailNotifier javaMailNotifier(@Value("${email.template.path:}") String customTemplatePath, JavaMailSender javaMailSender) {
            return new JavaMailNotifier(customTemplatePath, javaMailSender);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix="notification", name="engine", havingValue="none", matchIfMissing=true)
    @EnableAutoConfiguration(exclude = { MailSenderAutoConfiguration.class, SendGridAutoConfiguration.class })
    static class NoMailConfig {}

    @Configuration
    @ConditionalOnProperty(prefix="notification", name="engine", havingValue="sendgrid")
    @EnableAutoConfiguration(exclude = { MailSenderAutoConfiguration.class })
    static class SendGridConfig {

        @Bean
        public EmailNotifier sendGridNotifier(@Value("${email.template.path:}") String customTemplatePath, SendGrid sendGrid) {
            return new SendGridNotifier(customTemplatePath, sendGrid);
        }
    }
}
