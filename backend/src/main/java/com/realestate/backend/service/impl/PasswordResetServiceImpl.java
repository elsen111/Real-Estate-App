package com.realestate.backend.service.impl;

import com.realestate.backend.config.properties.PasswordResetProperties;
import com.realestate.backend.dto.request.ForgotPasswordRequest;
import com.realestate.backend.dto.request.VerifyResetOtpRequest;
import com.realestate.backend.dto.request.ResetPasswordRequest;
import com.realestate.backend.dto.response.VerifyResetOtpResponse;
import com.realestate.backend.entity.PasswordResetOtpEntity;
import com.realestate.backend.entity.PasswordResetTokenEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.repository.PasswordResetOtpRepository;
import com.realestate.backend.repository.PasswordResetTokenRepository;
import com.realestate.backend.repository.RefreshTokenRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.service.EmailService;
import com.realestate.backend.service.PasswordResetService;
import com.realestate.backend.utils.otp.OtpGenerator;
import com.realestate.backend.utils.otp.OtpHashUtil;
import com.realestate.backend.utils.resetToken.ResetTokenGenerator;
import com.realestate.backend.utils.resetToken.ResetTokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String INVALID_OTP_MESSAGE =
            "Invalid or expired OTP.";

    private static final String INVALID_TOKEN_MESSAGE =
            "Invalid or expired password reset token.";

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final OtpHashUtil otpHashUtil;
    private final ResetTokenGenerator resetTokenGenerator;
    private final ResetTokenHashUtil resetTokenHashUtil;

    private final PasswordResetProperties properties;

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        String email = normalizeEmail(request.email());

        UserEntity user = userRepository
                .findByEmailAndDeletedFalse(email)
                .orElse(null);

        if (user == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        PasswordResetOtpEntity latestOtp =
                passwordResetOtpRepository
                        .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                        .orElse(null);

        if (latestOtp != null) {

            LocalDateTime nextAllowedTime = latestOtp.getCreatedAt()
                    .plusSeconds(properties.otpResendCooldownSeconds());

            if (now.isBefore(nextAllowedTime)) {
                return;
            }
        }

        passwordResetOtpRepository.deleteByUser(user);
        passwordResetOtpRepository.flush();

        String rawOtp = OtpGenerator.generateOtp();

        PasswordResetOtpEntity otpEntity = PasswordResetOtpEntity.builder()
                .user(user)
                .otpHash(otpHashUtil.hash(rawOtp))
                .expiresAt(now.plusMinutes(properties.otpExpirationMinutes()))
                .attempts(0)
                .used(false)
                .build();

        passwordResetOtpRepository.save(otpEntity);

        emailService.sendPasswordResetOtp(
                user.getEmail(),
                rawOtp
        );

        log.atInfo()
                .setMessage("Password reset OTP generated")
                .addKeyValue("userId", user.getId())
                .log();
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public VerifyResetOtpResponse verifyResetOtp(
            VerifyResetOtpRequest request
    ) {

        String email = normalizeEmail(request.email());

        UserEntity user = userRepository
                .findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException(INVALID_OTP_MESSAGE));

        PasswordResetOtpEntity otpEntity =
                passwordResetOtpRepository
                        .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                        .orElseThrow(() -> new BadRequestException(INVALID_OTP_MESSAGE));

        LocalDateTime now = LocalDateTime.now();

        if (otpEntity.getExpiresAt().isBefore(now)) {
            passwordResetOtpRepository.delete(otpEntity);

            throw new BadRequestException(INVALID_OTP_MESSAGE);
        }

        if (otpEntity.getAttempts() >= properties.otpMaxAttempts()) {
            passwordResetOtpRepository.delete(otpEntity);

            throw new BadRequestException(INVALID_OTP_MESSAGE);
        }

        String submittedOtpHash = otpHashUtil.hash(request.otp());

        if (!java.security.MessageDigest.isEqual(
                submittedOtpHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                otpEntity.getOtpHash().getBytes(java.nio.charset.StandardCharsets.UTF_8)
        )) {

            otpEntity.setAttempts(otpEntity.getAttempts() + 1);

            if (otpEntity.getAttempts() >= properties.otpMaxAttempts()) {
                passwordResetOtpRepository.delete(otpEntity);
            } else {
                passwordResetOtpRepository.save(otpEntity);
            }

            throw new BadRequestException(INVALID_OTP_MESSAGE);
        }

        String rawResetToken = resetTokenGenerator.generate();

        PasswordResetTokenEntity resetTokenEntity =
                PasswordResetTokenEntity.builder()
                        .user(user)
                        .tokenHash(resetTokenHashUtil.hash(rawResetToken))
                        .expiresAt(now.plusMinutes(properties.tokenExpirationMinutes()))
                        .build();

        passwordResetTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.flush();

        passwordResetTokenRepository.save(resetTokenEntity);

        passwordResetOtpRepository.delete(otpEntity);

        log.atInfo()
                .setMessage("Password reset OTP verified")
                .addKeyValue("userId", user.getId())
                .log();

        return new VerifyResetOtpResponse(rawResetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        String tokenHash = resetTokenHashUtil.hash(request.getResetToken());

        PasswordResetTokenEntity resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() -> new BadRequestException(INVALID_TOKEN_MESSAGE));

        LocalDateTime now = LocalDateTime.now();

        if (resetToken.getExpiresAt().isBefore(now)) {
            throw new BadRequestException(INVALID_TOKEN_MESSAGE);
        }

        UserEntity user = resetToken.getUser();

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        refreshTokenRepository.deleteAllByUser(user);

        log.atInfo()
                .setMessage("Password reset completed")
                .addKeyValue("userId", user.getId())
                .log();
    }


    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}