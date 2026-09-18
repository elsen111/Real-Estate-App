package com.realestate.backend.service.impl;

import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.enums.SubscriptionNotificationType;
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
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy");

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

            log.atInfo()
                    .setMessage("Sending password reset OTP to the email address")
                    .addKeyValue("email", toEmail)
                    .log();

            mailSender.send(message);

            log.atInfo()
                    .setMessage("Password reset OTP sent to the email address")
                    .addKeyValue("email", toEmail)
                    .log();

        } catch (Exception e) {
            throw new BusinessException("Failed to send password reset OTP email");
        }
    }

    @Override
    public void sendSubscriptionExpirationEmail(
            AgencySubscriptionEntity subscription,
            SubscriptionNotificationType type
    ) {

        String toEmail =
                subscription.getAgency().getEmail();

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);

            helper.setSubject(
                    buildSubscriptionEmailSubject(type)
            );

            Context context = new Context();

            context.setVariable(
                    "agencyName",
                    subscription.getAgency().getName()
            );

            context.setVariable(
                    "planName",
                    subscription.getPlan().getName()
            );

            context.setVariable(
                    "endDate",
                    subscription.getEndDate()
                            .format(DATE_FORMATTER)
            );

            context.setVariable(
                    "notificationType",
                    type
            );

            String html =
                    templateEngine.process(
                            "subscription-expiration-email",
                            context
                    );

            helper.setText(html, true);

            log.atInfo()
                    .setMessage(
                            "Sending subscription expiration email"
                    )
                    .addKeyValue(
                            "subscriptionId",
                            subscription.getId()
                    )
                    .addKeyValue(
                            "agencyId",
                            subscription.getAgency().getId()
                    )
                    .addKeyValue(
                            "email",
                            toEmail
                    )
                    .addKeyValue(
                            "notificationType",
                            type
                    )
                    .log();

            mailSender.send(message);

            log.atInfo()
                    .setMessage(
                            "Subscription expiration email sent"
                    )
                    .addKeyValue(
                            "subscriptionId",
                            subscription.getId()
                    )
                    .addKeyValue(
                            "notificationType",
                            type
                    )
                    .log();

        } catch (Exception e) {

            throw new BusinessException(
                    "Failed to send subscription expiration email"
            );
        }
    }

    private String buildSubscriptionEmailSubject(
            SubscriptionNotificationType type
    ) {

        return switch (type) {

            case EXPIRING_IN_7_DAYS ->
                    "EstateFlow subscription expires in 7 days";

            case EXPIRING_IN_1_DAY ->
                    "EstateFlow subscription expires tomorrow";

            case EXPIRED ->
                    "EstateFlow subscription has expired";
        };
    }
}