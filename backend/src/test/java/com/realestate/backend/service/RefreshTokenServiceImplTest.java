package com.realestate.backend.service;

import com.realestate.backend.config.JwtConfig;
import com.realestate.backend.entity.RefreshTokenEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.repository.RefreshTokenRepository;
import com.realestate.backend.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtConfig jwtConfig;

    @InjectMocks private RefreshTokenServiceImpl service;

    @Test
    void createRefreshToken_savesEntity_withHashedToken() {
        UserEntity user = UserEntity.builder().email("user@test.com").enabled(true).build();
        when(jwtConfig.refreshTokenExpiration()).thenReturn(Duration.ofDays(18));

        RefreshTokenServiceImpl.CreatedRefreshToken result =
                service.createRefreshToken(user, "127.0.0.1", "JUnit");

        assertThat(result.rawToken()).isNotBlank();
        assertThat(result.entity().getTokenHash()).isNotEqualTo(result.rawToken()); // must be hashed, not raw
    }

    @Test
    void validateAndGet_throws_whenTokenNotFound() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateAndGet("some-raw-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void validateAndGet_throws_whenTokenExpiredOrRevoked() {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .user(UserEntity.builder().enabled(true).build())
                .revoked(true)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.validateAndGet("some-raw-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired or revoked");
    }

    @Test
    void validateAndGet_throws_whenUserDisabled() {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .user(UserEntity.builder().enabled(false).build())
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.validateAndGet("some-raw-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void hash_isConsistent_forSameInput() {
        String hash1 = service.hash("raw-token-value");
        String hash2 = service.hash("raw-token-value");

        assertThat(hash1).isEqualTo(hash2);
    }

    private static String anyString() {
        return org.mockito.ArgumentMatchers.anyString();
    }
}