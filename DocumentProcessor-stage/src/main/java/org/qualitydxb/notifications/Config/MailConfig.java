package org.qualitydxb.notifications.Config;

import org.qualitydxb.infrastructure.SystemProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("qualitydxb.com");
        mailSender.setPort(465);
        mailSender.setUsername(SystemProperties.getNotifyEmail());
        mailSender.setPassword(SystemProperties.getNotifyEmailPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "qualitydxb.com");
        props.put("mail.debug", "false");

        return mailSender;
    }
}
