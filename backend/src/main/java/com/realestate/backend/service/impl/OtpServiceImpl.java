package com.realestate.backend.service.impl;

import com.realestate.backend.entity.PasswordResetOtpEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.InvalidOtpException;
import com.realestate.backend.repository.PasswordResetOtpRepository;
import com.realestate.backend.service.EmailService;
import com.realestate.backend.service.OtpService;
import com.realestate.backend.utils.otp.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final int OTP_EXPIRY_MINUTES = 10;

    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void generateAndSendOtp(UserEntity user) {

        passwordResetOtpRepository.deleteByUser(user);

        String otp = OtpGenerator.generateOtp();

        PasswordResetOtpEntity entity = PasswordResetOtpEntity.builder()
                .user(user)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .build();

        passwordResetOtpRepository.save(entity);

        emailService.sendPasswordResetOtp(
                user.getEmail(),
                otp
        );
    }

    @Override
    @Transactional
    public void verifyOtp(UserEntity user, String otp) {

        PasswordResetOtpEntity entity =
                passwordResetOtpRepository
                        .findTopByUserAndOtpAndUsedFalseOrderByCreatedAtDesc(user, otp)
                        .orElseThrow(() ->
                                new InvalidOtpException("Invalid OTP."));

        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP has expired.");
        }

        entity.setUsed(true);

        passwordResetOtpRepository.save(entity);
    }
    
}
