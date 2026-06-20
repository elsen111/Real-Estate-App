package com.realestate.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    private String secretKey;

    private long accessTokenExpirationMinutes = 30;

    private long refreshTokenExpirationDays = 18;

    public Duration accessTokenExpiration() {
        return Duration.ofMinutes(accessTokenExpirationMinutes);
    }

    public Duration refreshTokenExpiration() {
        return Duration.ofDays(refreshTokenExpirationDays);
    }
}