package com.realestate.backend.service.impl;

import com.realestate.backend.config.JwtConfig;
import com.realestate.backend.entity.RefreshTokenEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl {

    private static final int TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CreatedRefreshToken createRefreshToken(
            UserEntity user,
            String ipAddress,
            String userAgent
    ) {
        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiryDate(LocalDateTime.now().plus(jwtConfig.refreshTokenExpiration()))
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(entity);

        return new CreatedRefreshToken(rawToken, entity);
    }

    @Transactional
    public CreatedRefreshToken rotateRefreshToken(
            String oldRawToken,
            String ipAddress,
            String userAgent
    ) {
        RefreshTokenEntity oldToken = validateAndGet(oldRawToken);

        String newRawToken = generateRawToken();
        String newTokenHash = hash(newRawToken);

        oldToken.revoke(newTokenHash);

        RefreshTokenEntity newToken = RefreshTokenEntity.builder()
                .user(oldToken.getUser())
                .tokenHash(newTokenHash)
                .expiryDate(LocalDateTime.now().plus(jwtConfig.refreshTokenExpiration()))
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(oldToken);
        refreshTokenRepository.save(newToken);

        return new CreatedRefreshToken(newRawToken, newToken);
    }

    @Transactional(readOnly = true)
    public RefreshTokenEntity validateAndGet(String rawToken) {
        String tokenHash = hash(rawToken);

        RefreshTokenEntity token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.isRevoked() || token.isExpired()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        if (!token.getUser().getEnabled()) {
            throw new UnauthorizedException("User account is disabled");
        }

        return token;
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String tokenHash = hash(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.revoke(null);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllUserRefreshTokens(UUID userId) {
        refreshTokenRepository.findAllByUser_IdAndRevokedFalse(userId)
                .forEach(token -> {
                    token.revoke(null);
                    refreshTokenRepository.save(token);
                });
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public record CreatedRefreshToken(String rawToken, RefreshTokenEntity entity) {
    }
}