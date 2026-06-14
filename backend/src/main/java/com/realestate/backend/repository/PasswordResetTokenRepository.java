package com.realestate.backend.repository;

import com.realestate.backend.entity.PasswordResetTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    Optional<PasswordResetTokenEntity> findByTokenAndUsedFalseAndExpiryDateAfter(
            String token,
            LocalDateTime currentDateTime
    );

    @Transactional
    void deleteByUser_Id(UUID userId);

}
