package com.realestate.backend.service;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.PaymentEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.exception.EmailSendingException;
import com.realestate.backend.service.impl.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private AgencyEntity agency;

    @Mock
    private SubscriptionPlanEntity plan;

    @Mock
    private PaymentEntity payment;

    @Mock
    private AgencySubscriptionEntity subscription;

    @InjectMocks
    private EmailServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                service,
                "fromEmail",
                "noreply@estateflow.test"
        );

        ReflectionTestUtils.setField(
                service,
                "fromName",
                "EstateFlow"
        );
    }

    @Test
    void sendPasswordResetOtp_sendsMimeMessage_whenTemplateRendersSuccessfully()
            throws Exception {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(
                eq("password-reset-email"),
                any(Context.class)
        )).thenReturn("<html>OTP</html>");

        service.sendPasswordResetOtp(
                "user@test.com",
                "123456"
        );

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetOtp_throwsBusinessException_whenSendingFails() {

        when(mailSender.createMimeMessage())
                .thenThrow(new RuntimeException("SMTP down"));

        assertThatThrownBy(() ->
                service.sendPasswordResetOtp(
                        "user@test.com",
                        "123456"
                )
        )
                .isInstanceOf(EmailSendingException.class)
                .hasMessageContaining("Failed to send");
    }

    @Test
    void sendPaymentSuccessEmail_sendsMimeMessage_whenTemplateRendersSuccessfully()
            throws Exception {

        UUID paymentId = UUID.randomUUID();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(payment.getAgency()).thenReturn(agency);
        when(payment.getPlan()).thenReturn(plan);
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getAmount()).thenReturn(new BigDecimal("99.99"));
        when(payment.getCurrency()).thenReturn("azn");

        when(agency.getEmail()).thenReturn("agency@test.com");
        when(agency.getName()).thenReturn("Test Agency");

        when(plan.getName()).thenReturn("Premium");

        when(subscription.getStartDate())
                .thenReturn(LocalDate.of(2026, 9, 24));

        when(subscription.getEndDate())
                .thenReturn(LocalDate.of(2026, 10, 24));

        when(templateEngine.process(
                eq("payment-success-email"),
                any(Context.class)
        )).thenReturn("<html>Payment successful</html>");

        service.sendPaymentSuccessEmail(
                payment,
                subscription
        );

        verify(mailSender).send(mimeMessage);

        verify(templateEngine).process(
                eq("payment-success-email"),
                any(Context.class)
        );
    }

    @Test
    void sendPaymentSuccessEmail_passesExpectedContextVariables_whenTemplateRendersSuccessfully()
            throws Exception {

        UUID paymentId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("149.50");
        String currency = "usd";
        String agencyName = "Test Agency";
        String planName = "Professional";

        LocalDate startDate = LocalDate.of(2026, 9, 24);
        LocalDate endDate = LocalDate.of(2026, 10, 24);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(payment.getAgency()).thenReturn(agency);
        when(payment.getPlan()).thenReturn(plan);
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getAmount()).thenReturn(amount);
        when(payment.getCurrency()).thenReturn(currency);

        when(agency.getEmail()).thenReturn("agency@test.com");
        when(agency.getName()).thenReturn(agencyName);

        when(plan.getName()).thenReturn(planName);

        when(subscription.getStartDate()).thenReturn(startDate);
        when(subscription.getEndDate()).thenReturn(endDate);

        when(templateEngine.process(
                eq("payment-success-email"),
                any(Context.class)
        )).thenReturn("<html>Payment successful</html>");

        service.sendPaymentSuccessEmail(
                payment,
                subscription
        );

        ArgumentCaptor<Context> contextCaptor =
                ArgumentCaptor.forClass(Context.class);

        verify(templateEngine).process(
                eq("payment-success-email"),
                contextCaptor.capture()
        );

        Context context = contextCaptor.getValue();

        assertThat(context.getVariable("agencyName"))
                .isEqualTo(agencyName);

        assertThat(context.getVariable("planName"))
                .isEqualTo(planName);

        assertThat(context.getVariable("amount"))
                .isEqualTo(amount);

        assertThat(context.getVariable("currency"))
                .isEqualTo("USD");

        assertThat(context.getVariable("paymentId"))
                .isEqualTo(paymentId);

        assertThat(context.getVariable("startDate"))
                .isEqualTo("24 September 2026");

        assertThat(context.getVariable("endDate"))
                .isEqualTo("24 October 2026");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPaymentSuccessEmail_throwsBusinessException_whenSendingFails() {

        when(payment.getAgency()).thenReturn(agency);

        when(mailSender.createMimeMessage())
                .thenThrow(new RuntimeException("SMTP down"));

        assertThatThrownBy(() ->
                service.sendPaymentSuccessEmail(
                        payment,
                        subscription
                )
        )
                .isInstanceOf(EmailSendingException.class)
                .hasMessageContaining(
                        "Failed to send payment success email"
                );
    }

    @Test
    void sendPaymentFailureEmail_sendsMimeMessage_whenTemplateRendersSuccessfully()
            throws Exception {

        UUID paymentId = UUID.randomUUID();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(payment.getAgency()).thenReturn(agency);
        when(payment.getPlan()).thenReturn(plan);
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getAmount()).thenReturn(new BigDecimal("49.99"));
        when(payment.getCurrency()).thenReturn("azn");

        when(agency.getEmail()).thenReturn("agency@test.com");
        when(agency.getName()).thenReturn("Test Agency");

        when(plan.getName()).thenReturn("Basic");

        when(templateEngine.process(
                eq("payment-failed-email"),
                any(Context.class)
        )).thenReturn("<html>Payment failed</html>");

        service.sendPaymentFailureEmail(payment);

        verify(mailSender).send(mimeMessage);

        verify(templateEngine).process(
                eq("payment-failed-email"),
                any(Context.class)
        );
    }

    @Test
    void sendPaymentFailureEmail_passesExpectedContextVariables_whenTemplateRendersSuccessfully()
            throws Exception {

        UUID paymentId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("79.99");
        String currency = "usd";
        String agencyName = "Test Agency";
        String planName = "Premium";
        String failureReason = "Payment was declined by the card issuer";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(payment.getAgency()).thenReturn(agency);
        when(payment.getPlan()).thenReturn(plan);
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getAmount()).thenReturn(amount);
        when(payment.getCurrency()).thenReturn(currency);
        when(payment.getFailureReason()).thenReturn(failureReason);

        when(agency.getEmail()).thenReturn("agency@test.com");
        when(agency.getName()).thenReturn(agencyName);

        when(plan.getName()).thenReturn(planName);

        when(templateEngine.process(
                eq("payment-failed-email"),
                any(Context.class)
        )).thenReturn("<html>Payment failed</html>");

        service.sendPaymentFailureEmail(payment);

        ArgumentCaptor<Context> contextCaptor =
                ArgumentCaptor.forClass(Context.class);

        verify(templateEngine).process(
                eq("payment-failed-email"),
                contextCaptor.capture()
        );

        Context context = contextCaptor.getValue();

        assertThat(context.getVariable("agencyName"))
                .isEqualTo(agencyName);

        assertThat(context.getVariable("planName"))
                .isEqualTo(planName);

        assertThat(context.getVariable("amount"))
                .isEqualTo(amount);

        assertThat(context.getVariable("currency"))
                .isEqualTo("USD");

        assertThat(context.getVariable("paymentId"))
                .isEqualTo(paymentId);

        assertThat(context.getVariable("status"))
                .isEqualTo(null);

        assertThat(context.getVariable("failureReason"))
                .isEqualTo(failureReason);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPaymentFailureEmail_throwsBusinessException_whenSendingFails() {

        when(payment.getAgency()).thenReturn(agency);

        when(mailSender.createMimeMessage())
                .thenThrow(new RuntimeException("SMTP down"));

        assertThatThrownBy(() ->
                service.sendPaymentFailureEmail(payment)
        )
                .isInstanceOf(EmailSendingException.class)
                .hasMessageContaining(
                        "Failed to send payment failure email"
                );
    }
}