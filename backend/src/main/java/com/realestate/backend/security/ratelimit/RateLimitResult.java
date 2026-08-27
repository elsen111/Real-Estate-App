package com.realestate.backend.security.ratelimit;

public record RateLimitResult(
        boolean allowed,
        long remainingTokens,
        long retryAfterSeconds
) {
}