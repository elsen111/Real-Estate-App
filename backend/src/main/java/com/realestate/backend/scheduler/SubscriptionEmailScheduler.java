package com.realestate.backend.scheduler;

import com.realestate.backend.service.SubscriptionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEmailScheduler {

    private final SubscriptionNotificationService subscriptionNotificationService;

    @Scheduled(
            cron = "${subscription.scheduler.email-cron}",
            zone = "${subscription.scheduler.time-zone}"
    )
    public void processEmails() {
        log.debug("Starting subscription email scheduler");

        subscriptionNotificationService.processPendingNotifications();

        log.debug("Subscription email scheduler completed");
    }
}