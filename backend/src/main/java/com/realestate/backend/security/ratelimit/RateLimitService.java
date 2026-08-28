package com.realestate.backend.security.ratelimit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String EMAIL_FIELD = "email";

    private final RateLimitBucketRegistry bucketRegistry;
    private final ObjectMapper objectMapper;

    public RateLimitResult checkLimit(HttpServletRequest request) {
        RateLimitPolicy policy = RateLimitPolicy.resolve(request.getServletPath());

        String identity = policy == RateLimitPolicy.AUTH
                ? resolveAuthIdentity(request)
                : "ip:" + resolveClientIp(request);

        String bucketKey = policy.name() + ":" + identity;

        Bucket bucket = bucketRegistry.resolveBucket(bucketKey, policy);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        long retryAfterSeconds = probe.isConsumed()
                ? 0
                : TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;

        if (probe.isConsumed()) {
            log.debug(
                    "Rate limit check passed: policy={}, remainingTokens={}",
                    policy,
                    probe.getRemainingTokens()
            );
        } else {
            log.warn(
                    "Rate limit exceeded: policy={}, retryAfterSeconds={}",
                    policy,
                    retryAfterSeconds
            );
        }

        return new RateLimitResult(probe.isConsumed(), probe.getRemainingTokens(), retryAfterSeconds);
    }

    private String resolveAuthIdentity(HttpServletRequest request) {
        String email = extractEmail(request);
        return StringUtils.hasText(email)
                ? "email:" + email.trim().toLowerCase()
                : "ip:" + resolveClientIp(request);
    }

    private String extractEmail(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE)) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(request.getInputStream());
            JsonNode emailNode = root.get(EMAIL_FIELD);
            return (emailNode != null && emailNode.isTextual()) ? emailNode.asText() : null;
        } catch (IOException ex) {
            log.debug(
                    "Unable to extract rate-limit identity from request body: method={}, path={}",
                    request.getMethod(),
                    request.getServletPath(),
                    ex
            );

            return null;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String forwarded = request.getHeader("Forwarded");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}