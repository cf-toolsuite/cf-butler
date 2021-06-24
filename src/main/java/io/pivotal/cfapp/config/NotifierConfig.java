package io.pivotal.cfapp.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.sendgrid.SendGridAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import com.sendgrid.SendGrid;

import io.pivotal.cfapp.notifier.EmailNotifier;
import io.pivotal.cfapp.notifier.JavaMailNotifier;
import io.pivotal.cfapp.notifier.SendGridNotifier;

@Configuration
public class NotifierConfig {

    @Configuration
    @ConditionalOnProperty(prefix="notification", name="engine", havingValue="java-mail")
    @EnableAutoConfiguration(exclude = { SendGridAutoConfiguration.class })
    static class MailConfig {

        @Bean
        public EmailNotifier javaMailNotifier(JavaMailSender javaMailSender) {
            return new JavaMailNotifier(javaMailSender);
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
        public EmailNotifier sendGridNotifier(SendGrid sendGrid) {
            return new SendGridNotifier(sendGrid);
        }
    }
}
