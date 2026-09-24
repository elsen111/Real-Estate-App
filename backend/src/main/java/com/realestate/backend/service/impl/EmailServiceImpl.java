package com.realestate.backend.service.impl;

import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.PaymentEntity;
import com.realestate.backend.enums.SubscriptionNotificationType;
import com.realestate.backend.exception.EmailSendingException;
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
    public void sendPasswordResetOtp(
            String toEmail,
            String otp
    ) {

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
                    "EstateFlow Password Reset OTP"
            );

            Context context = new Context();
            context.setVariable("otp", otp);

            String html =
                    templateEngine.process(
                            "password-reset-email",
                            context
                    );

            helper.setText(html, true);

            mailSender.send(message);

            log.atInfo()
                    .setMessage(
                            "Password reset OTP email sent"
                    )
                    .addKeyValue(
                            "email",
                            toEmail
                    )
                    .log();

        } catch (Exception e) {

            throw new EmailSendingException(
                    "Failed to send password reset OTP email"
            );
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

            throw new EmailSendingException(
                    "Failed to send subscription expiration email"
            );
        }
    }

    @Override
    public void sendPaymentSuccessEmail(
            PaymentEntity payment,
            AgencySubscriptionEntity subscription
    ) {

        String toEmail =
                payment.getAgency().getEmail();

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
                    "EstateFlow subscription payment successful"
            );

            Context context = new Context();

            context.setVariable(
                    "agencyName",
                    payment.getAgency().getName()
            );

            context.setVariable(
                    "planName",
                    payment.getPlan().getName()
            );

            context.setVariable(
                    "amount",
                    payment.getAmount()
            );

            context.setVariable(
                    "currency",
                    payment.getCurrency().toUpperCase()
            );

            context.setVariable(
                    "paymentId",
                    payment.getId()
            );

            context.setVariable(
                    "startDate",
                    subscription.getStartDate()
                            .format(DATE_FORMATTER)
            );

            context.setVariable(
                    "endDate",
                    subscription.getEndDate()
                            .format(DATE_FORMATTER)
            );

            String html =
                    templateEngine.process(
                            "payment-success-email",
                            context
                    );

            helper.setText(html, true);

            mailSender.send(message);

            log.atInfo()
                    .setMessage(
                            "Payment success email sent"
                    )
                    .addKeyValue(
                            "paymentId",
                            payment.getId()
                    )
                    .addKeyValue(
                            "agencyId",
                            payment.getAgency().getId()
                    )
                    .log();

        } catch (Exception e) {

            throw new EmailSendingException(
                    "Failed to send payment success email"
            );
        }
    }

    @Override
    public void sendPaymentFailureEmail(
            PaymentEntity payment
    ) {

        String toEmail =
                payment.getAgency().getEmail();

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
                    "EstateFlow payment was not successful"
            );

            Context context = new Context();

            context.setVariable(
                    "agencyName",
                    payment.getAgency().getName()
            );

            context.setVariable(
                    "planName",
                    payment.getPlan().getName()
            );

            context.setVariable(
                    "amount",
                    payment.getAmount()
            );

            context.setVariable(
                    "currency",
                    payment.getCurrency().toUpperCase()
            );

            context.setVariable(
                    "paymentId",
                    payment.getId()
            );

            context.setVariable(
                    "status",
                    payment.getStatus()
            );

            context.setVariable(
                    "failureReason",
                    payment.getFailureReason()
            );

            String html =
                    templateEngine.process(
                            "payment-failed-email",
                            context
                    );

            helper.setText(html, true);

            mailSender.send(message);

            log.atInfo()
                    .setMessage(
                            "Payment failure email sent"
                    )
                    .addKeyValue(
                            "paymentId",
                            payment.getId()
                    )
                    .addKeyValue(
                            "agencyId",
                            payment.getAgency().getId()
                    )
                    .log();

        } catch (Exception e) {

            throw new EmailSendingException(
                    "Failed to send payment failure email"
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