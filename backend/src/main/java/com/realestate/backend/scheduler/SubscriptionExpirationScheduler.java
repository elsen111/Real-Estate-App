package com.realestate.backend.scheduler;

import com.realestate.backend.service.SubscriptionExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpirationScheduler {

    private final SubscriptionExpirationService subscriptionExpirationService;

    @Scheduled(
            cron = "${subscription.scheduler.expiration-cron}",
            zone = "${subscription.scheduler.time-zone}"
    )
    public void processSubscriptions() {
        log.debug("Starting subscription expiration scheduler");

        subscriptionExpirationService.processSubscriptions();

        log.debug("Subscription expiration scheduler completed");
    }
}