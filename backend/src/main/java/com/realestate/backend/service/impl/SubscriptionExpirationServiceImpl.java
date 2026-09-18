package com.realestate.backend.service.impl;

import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.enums.SubscriptionNotificationType;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.repository.AgencySubscriptionRepository;
import com.realestate.backend.service.SubscriptionExpirationService;
import com.realestate.backend.service.SubscriptionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionExpirationServiceImpl
        implements SubscriptionExpirationService {

    private static final int BATCH_SIZE = 100;

    private final AgencySubscriptionRepository
            subscriptionRepository;

    private final SubscriptionNotificationService
            notificationService;

    @Override
    public void processSubscriptions() {

        LocalDate today = LocalDate.now();

        processExpiredSubscriptions(today);

        processSevenDayReminders(today);

        processOneDayReminders(today);
    }

    private void processExpiredSubscriptions(
            LocalDate today
    ) {

        List<AgencySubscriptionEntity> subscriptions =
                subscriptionRepository.findByStatusAndEndDateBefore(
                        SubscriptionStatus.ACTIVE,
                        today,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (AgencySubscriptionEntity subscription : subscriptions) {

            expireSubscription(subscription);
        }
    }

    private void processSevenDayReminders(
            LocalDate today
    ) {

        LocalDate targetDate =
                today.plusDays(7);

        List<AgencySubscriptionEntity> subscriptions =
                subscriptionRepository.findByStatusAndEndDate(
                        SubscriptionStatus.ACTIVE,
                        targetDate,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (AgencySubscriptionEntity subscription : subscriptions) {

            notificationService.createIfNotExists(
                    subscription,
                    SubscriptionNotificationType.EXPIRING_IN_7_DAYS
            );
        }
    }

    private void processOneDayReminders(
            LocalDate today
    ) {

        LocalDate targetDate =
                today.plusDays(1);

        List<AgencySubscriptionEntity> subscriptions =
                subscriptionRepository.findByStatusAndEndDate(
                        SubscriptionStatus.ACTIVE,
                        targetDate,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (AgencySubscriptionEntity subscription : subscriptions) {

            notificationService.createIfNotExists(
                    subscription,
                    SubscriptionNotificationType.EXPIRING_IN_1_DAY
            );
        }
    }

    @Transactional
    protected void expireSubscription(
            AgencySubscriptionEntity subscription
    ) {

        subscription.setStatus(
                SubscriptionStatus.EXPIRED
        );

        subscriptionRepository.save(subscription);

        notificationService.createIfNotExists(
                subscription,
                SubscriptionNotificationType.EXPIRED
        );

        log.atInfo()
                .setMessage("Subscription expired")
                .addKeyValue(
                        "subscriptionId",
                        subscription.getId()
                )
                .addKeyValue(
                        "agencyId",
                        subscription.getAgency().getId()
                )
                .addKeyValue(
                        "endDate",
                        subscription.getEndDate()
                )
                .log();
    }
}