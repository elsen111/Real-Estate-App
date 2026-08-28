package com.realestate.backend.security.ratelimit;

import com.realestate.backend.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitBucketRegistry {

    private final RateLimitProperties properties;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, RateLimitPolicy policy) {
        return buckets.computeIfAbsent(key, k -> newBucket(policy));
    }

    private Bucket newBucket(RateLimitPolicy policy) {
        RateLimitProperties.Limit limit = policy == RateLimitPolicy.AUTH
                ? properties.getAuth()
                : properties.getGeneral();

        Bandwidth bandwidth = Bandwidth.classic(
                limit.getCapacity(),
                Refill.greedy(limit.getCapacity(), Duration.ofMinutes(limit.getRefillDurationMinutes()))
        );

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}