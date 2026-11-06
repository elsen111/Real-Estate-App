package com.realestate.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.password-reset")
public record PasswordResetProperties(
        long otpExpirationMinutes,
        long otpResendCooldownSeconds,
        int otpMaxAttempts,
        long tokenExpirationMinutes,
        String otpHmacSecret
) {}