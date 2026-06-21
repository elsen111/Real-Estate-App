package com.realestate.backend.repository;

import com.realestate.backend.entity.RefreshTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    Optional<RefreshTokenEntity> findByTokenHashAndRevokedFalseAndExpiryDateAfter(
            String tokenHash,
            LocalDateTime currentDateTime
    );
    List<RefreshTokenEntity> findAllByUser_IdAndRevokedFalse(UUID userId);

    @Transactional
    void deleteByUser_Id(UUID userId);

}
