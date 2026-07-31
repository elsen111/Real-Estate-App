package com.realestate.backend.service;

import com.realestate.backend.entity.PasswordResetOtpEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.InvalidOtpException;
import com.realestate.backend.repository.PasswordResetOtpRepository;
import com.realestate.backend.service.impl.OtpServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock private PasswordResetOtpRepository passwordResetOtpRepository;
    @Mock private EmailService emailService;

    @InjectMocks private OtpServiceImpl service;

    @Test
    void generateAndSendOtp_deletesOldOtps_savesNewOne_andSendsEmail() {
        UserEntity user = UserEntity.builder().email("user@test.com").build();

        service.generateAndSendOtp(user);

        verify(passwordResetOtpRepository).deleteByUser(user);
        ArgumentCaptor<PasswordResetOtpEntity> captor = ArgumentCaptor.forClass(PasswordResetOtpEntity.class);
        verify(passwordResetOtpRepository).save(captor.capture());
        assertThat(captor.getValue().getOtp()).hasSize(6);
        verify(emailService).sendPasswordResetOtp(eq("user@test.com"), any());
    }

    @Test
    void verifyOtp_throws_whenNoMatchingOtpFound() {
        UserEntity user = UserEntity.builder().email("user@test.com").build();
        when(passwordResetOtpRepository.findTopByUserAndOtpAndUsedFalseOrderByCreatedAtDesc(user, "000000"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyOtp(user, "000000"))
                .isInstanceOf(InvalidOtpException.class);
    }

    @Test
    void verifyOtp_throws_whenOtpExpired() {
        UserEntity user = UserEntity.builder().email("user@test.com").build();
        PasswordResetOtpEntity expiredOtp = PasswordResetOtpEntity.builder()
                .otp("123456").expiresAt(LocalDateTime.now().minusMinutes(1)).used(false).build();

        when(passwordResetOtpRepository.findTopByUserAndOtpAndUsedFalseOrderByCreatedAtDesc(user, "123456"))
                .thenReturn(Optional.of(expiredOtp));

        assertThatThrownBy(() -> service.verifyOtp(user, "123456"))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void verifyOtp_marksOtpUsed_whenValidAndNotExpired() {
        UserEntity user = UserEntity.builder().email("user@test.com").build();
        PasswordResetOtpEntity validOtp = PasswordResetOtpEntity.builder()
                .otp("123456").expiresAt(LocalDateTime.now().plusMinutes(5)).used(false).build();

        when(passwordResetOtpRepository.findTopByUserAndOtpAndUsedFalseOrderByCreatedAtDesc(user, "123456"))
                .thenReturn(Optional.of(validOtp));

        service.verifyOtp(user, "123456");

        assertThat(validOtp.getUsed()).isTrue();
        verify(passwordResetOtpRepository).save(validOtp);
    }
}