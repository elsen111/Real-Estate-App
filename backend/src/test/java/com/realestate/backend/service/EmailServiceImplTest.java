package com.realestate.backend.service;

import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.service.impl.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;

    @InjectMocks private EmailServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "fromEmail", "noreply@estateflow.test");
        ReflectionTestUtils.setField(service, "fromName", "EstateFlow");
    }

    @Test
    void sendPasswordResetOtp_sendsMimeMessage_whenTemplateRendersSuccessfully() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("password-reset-email"), any())).thenReturn("<html>OTP</html>");

        service.sendPasswordResetOtp("user@test.com", "123456");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetOtp_throwsBusinessException_whenSendingFails() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP down"));

        assertThatThrownBy(() -> service.sendPasswordResetOtp("user@test.com", "123456"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to send");
    }
}