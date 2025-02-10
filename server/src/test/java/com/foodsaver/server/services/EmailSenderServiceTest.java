package com.foodsaver.server.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;
import static org.mockito.Mockito.*;

class EmailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private Logger logger;

    @InjectMocks
    private EmailSenderService emailSenderService;

    @Value("${spring.mail.username}")
    private String recipientEmail = "test@example.com";

    @Value("${spring.mail.password}")
    private String recipientPassword = "password";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void configureMailSender_ShouldSetMailSenderProperties() {
        JavaMailSenderImpl mailSenderImpl = mock(JavaMailSenderImpl.class);
        Properties mockProperties = mock(Properties.class);

        when(mailSenderImpl.getJavaMailProperties()).thenReturn(mockProperties);
        emailSenderService = new EmailSenderService(mailSenderImpl, recipientEmail, recipientPassword);

        emailSenderService.configureMailSender();

        verify(mailSenderImpl).setUsername(recipientEmail);
        verify(mailSenderImpl).setPassword(recipientPassword);
        verify(mockProperties).put("mail.smtp.auth", "true");
        verify(mockProperties).put("mail.smtp.starttls.enable", "true");
    }

}

