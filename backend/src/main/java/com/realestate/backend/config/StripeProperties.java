package com.realestate.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.stripe")
public record StripeProperties(
        String secretKey,
        String webhookSecret,
        String currency,
        String successUrl,
        String cancelUrl
) {
    public StripeProperties {
        secretKey = secretKey == null ? null : secretKey.trim();
        webhookSecret = webhookSecret == null ? null : webhookSecret.trim();
    }
}