package com.realestate.backend.repository;

import com.realestate.backend.entity.PasswordResetOtpEntity;
import com.realestate.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtpEntity, UUID> {

    Optional<PasswordResetOtpEntity> findTopByUserOrderByCreatedAtDesc(
            UserEntity user
    );

    Optional<PasswordResetOtpEntity> findTopByUserAndOtpAndUsedFalseOrderByCreatedAtDesc(
            UserEntity user,
            String otp
    );

    List<PasswordResetOtpEntity> findByUser(UserEntity user);

    void deleteByUser(UserEntity user);

    Optional<PasswordResetOtpEntity>
    findTopByUserAndUsedFalseOrderByCreatedAtDesc(UserEntity user);

}
