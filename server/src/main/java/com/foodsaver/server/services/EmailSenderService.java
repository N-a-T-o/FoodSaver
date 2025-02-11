package com.foodsaver.server.services;

import com.foodsaver.server.dtos.VerificationTokenDTO;
import com.foodsaver.server.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class EmailSenderService {
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String recipientEmail;

    @Value("${spring.mail.password}")
    private String recipientPassword;

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);

    public void sendVerificationToken(VerificationTokenDTO token, User user) {
        try {
            String body = createVerificationEmailBody(token, user);
            sendToEmail(user.getEmail(), body, "Активационен код");
            logger.info("Verification email sent to " + user.getEmail());
        } catch (MessagingException e) {
            logger.error("An error occurred while sending a verification email", e);
        }
    }

    private void sendToEmail(String email, String body, String subject) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setFrom(recipientEmail);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

    private String createVerificationEmailBody(VerificationTokenDTO token, User user) {
        String username = user.getUsername();
        String email = user.getEmail();

        LocalDateTime expiryDate = token.getExpiryDate().minusMinutes(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm");
        String formattedDate = expiryDate.format(formatter);

        return "<div style='text-align: center;'>"
                + "<h2>Здравейте " + username + ",</h2>"
                + "<p>Получавате този имейл, защото на " + formattedDate + " беше извършена регистрация с вашия имейл: <strong>" + email + "</strong>. Ако не сте извършвали регистрация, игнорирайте имейла.</p>"
                + "<p>Вашият код за активация на профила Ви е: <strong>" + token.getToken() + "</strong></p>"
                + "<p>Използвайте този код, за да активирате Вашия профил във СъХРАНИтел.</p>"
                + "<p>Пожелаваме Ви успех,<p/>"
                + "<p>Екип на СъХРАНИтел</p>"
                + "</div>";
    }

    @PostConstruct
    public void configureMailSender() {
        if (mailSender instanceof JavaMailSenderImpl) {
            JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
            mailSenderImpl.setUsername(recipientEmail);
            mailSenderImpl.setPassword(recipientPassword);

            Properties mailProperties = mailSenderImpl.getJavaMailProperties();
            mailProperties.put("mail.smtp.auth", "true");
            mailProperties.put("mail.smtp.starttls.enable", "true");
        }
    }
}
