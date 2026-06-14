package com.realestate.backend.repository;

import com.realestate.backend.entity.RefreshTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    Optional<RefreshTokenEntity> findByTokenAndRevokedFalseAndExpiryDateAfter(
            String token,
            LocalDateTime currentDateTime
    );

    @Transactional
    void deleteByUser_Id(UUID userId);

}
