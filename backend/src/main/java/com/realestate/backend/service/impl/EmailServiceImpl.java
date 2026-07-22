package com.realestate.backend.service.impl;

import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Override
    public void sendPasswordResetOtp(String toEmail, String otp) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("EstateFlow Password Reset OTP");

            Context context = new Context();
            context.setVariable("otp", otp);

            String html = templateEngine.process(
                    "password-reset-email",
                    context
            );

            helper.setText(html, true);

            log.info("Sending password reset OTP email to {}", toEmail);

            mailSender.send(message);

            log.info("Password reset OTP email sent successfully to {}", toEmail);

        } catch (Exception e) {
            throw new BusinessException("Failed to send password reset OTP email");
        }
    }
}