package com.realestate.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private final Limit auth = new Limit(5, 1);
    private final Limit general = new Limit(100, 1);

    @Getter
    @Setter
    public static class Limit {

        private long capacity;

        private long refillDurationMinutes;

        public Limit() {
        }

        public Limit(long capacity, long refillDurationMinutes) {
            this.capacity = capacity;
            this.refillDurationMinutes = refillDurationMinutes;
        }
    }
}