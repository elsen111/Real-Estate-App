package com.realestate.backend.repository;

import com.realestate.backend.entity.PasswordResetOtpEntity;
import com.realestate.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtpEntity, UUID> {

    Optional<PasswordResetOtpEntity>
    findTopByUserAndUsedFalseOrderByCreatedAtDesc(UserEntity user);

    void deleteByUser(UserEntity user);
}