package com.realestate.backend.service.impl;

import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.SubscriptionNotificationEntity;
import com.realestate.backend.enums.NotificationStatus;
import com.realestate.backend.enums.SubscriptionNotificationType;
import com.realestate.backend.repository.SubscriptionNotificationRepository;
import com.realestate.backend.service.EmailService;
import com.realestate.backend.service.SubscriptionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionNotificationServiceImpl
        implements SubscriptionNotificationService {

    private static final int BATCH_SIZE = 50;

    private final SubscriptionNotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createIfNotExists(
            AgencySubscriptionEntity subscription,
            SubscriptionNotificationType type
    ) {

        boolean exists =
                notificationRepository.existsBySubscriptionIdAndType(
                        subscription.getId(),
                        type
                );

        if (exists) {
            return;
        }

        SubscriptionNotificationEntity notification =
                SubscriptionNotificationEntity.builder()
                        .subscription(subscription)
                        .type(type)
                        .status(NotificationStatus.PENDING)
                        .attempts(0)
                        .build();

        try {

            notificationRepository.save(notification);

            log.atInfo()
                    .setMessage("Subscription notification created")
                    .addKeyValue(
                            "subscriptionId",
                            subscription.getId()
                    )
                    .addKeyValue("notificationType", type)
                    .log();

        } catch (DataIntegrityViolationException e) {

            log.atDebug()
                    .setMessage(
                            "Subscription notification already exists"
                    )
                    .addKeyValue(
                            "subscriptionId",
                            subscription.getId()
                    )
                    .addKeyValue("notificationType", type)
                    .log();
        }
    }

    @Override
    public void processPendingNotifications() {

        List<SubscriptionNotificationEntity> notifications =
                notificationRepository.findByStatusOrderByCreatedAtAsc(
                        NotificationStatus.PENDING,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (SubscriptionNotificationEntity notification : notifications) {

            processNotification(notification);
        }
    }

    private void processNotification(
            SubscriptionNotificationEntity notification
    ) {

        int claimed = notificationRepository.claimNotification(
                notification.getId(),
                NotificationStatus.PENDING,
                NotificationStatus.PROCESSING,
                LocalDateTime.now()
        );

        if (claimed == 0) {
            return;
        }

        try {

            AgencySubscriptionEntity subscription =
                    notification.getSubscription();

            emailService.sendSubscriptionExpirationEmail(
                    subscription,
                    notification.getType()
            );

            markAsSent(notification);

        } catch (Exception e) {

            markAsFailed(notification, e);

        }
    }

    @Transactional
    protected void markAsSent(
            SubscriptionNotificationEntity notification
    ) {

        notification.setStatus(
                NotificationStatus.SENT
        );

        notification.setSentAt(LocalDateTime.now());
        notification.setLockedAt(null);
        notification.setLastError(null);

        notificationRepository.save(notification);

        log.atInfo()
                .setMessage("Subscription notification sent")
                .addKeyValue(
                        "notificationId",
                        notification.getId()
                )
                .addKeyValue(
                        "notificationType",
                        notification.getType()
                )
                .log();
    }

    @Transactional
    protected void markAsFailed(
            SubscriptionNotificationEntity notification,
            Exception exception
    ) {

        notification.setStatus(
                NotificationStatus.FAILED
        );

        notification.setLockedAt(null);
        notification.setLastError(
                exception.getMessage()
        );

        notificationRepository.save(notification);

        log.atError()
                .setMessage("Subscription notification failed")
                .addKeyValue(
                        "notificationId",
                        notification.getId()
                )
                .addKeyValue(
                        "notificationType",
                        notification.getType()
                )
                .setCause(exception)
                .log();
    }

    @Override
    @Transactional
    public void releaseStuckNotifications() {

        LocalDateTime threshold =
                LocalDateTime.now().minusMinutes(10);

        int released =
                notificationRepository.releaseStuckNotifications(
                        NotificationStatus.PROCESSING,
                        NotificationStatus.PENDING,
                        threshold
                );

        if (released > 0) {

            log.atWarn()
                    .setMessage(
                            "Released stuck subscription notifications"
                    )
                    .addKeyValue("count", released)
                    .log();
        }
    }
}