package com.realestate.backend.service;

import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.SubscriptionNotificationEntity;
import com.realestate.backend.enums.NotificationStatus;
import com.realestate.backend.enums.SubscriptionNotificationType;
import com.realestate.backend.repository.SubscriptionNotificationRepository;
import com.realestate.backend.service.impl.SubscriptionNotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionNotificationServiceImplTest {

    @Mock
    private SubscriptionNotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SubscriptionNotificationServiceImpl notificationService;


    @Test
    void createIfNotExists_shouldCreateNotificationWhenNotExists() {

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        SubscriptionNotificationType type =
                SubscriptionNotificationType.EXPIRING_IN_7_DAYS;

        when(
                notificationRepository.existsBySubscriptionIdAndType(
                        subscription.getId(),
                        type
                )
        ).thenReturn(false);

        notificationService.createIfNotExists(
                subscription,
                type
        );

        ArgumentCaptor<SubscriptionNotificationEntity> captor =
                ArgumentCaptor.forClass(
                        SubscriptionNotificationEntity.class
                );

        verify(notificationRepository).save(
                captor.capture()
        );

        SubscriptionNotificationEntity savedNotification =
                captor.getValue();

        assertEquals(
                subscription,
                savedNotification.getSubscription()
        );

        assertEquals(
                type,
                savedNotification.getType()
        );

        assertEquals(
                NotificationStatus.PENDING,
                savedNotification.getStatus()
        );

        assertEquals(
                0,
                savedNotification.getAttempts()
        );
    }


    @Test
    void createIfNotExists_shouldNotCreateNotificationWhenAlreadyExists() {

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        SubscriptionNotificationType type =
                SubscriptionNotificationType.EXPIRING_IN_7_DAYS;

        when(
                notificationRepository.existsBySubscriptionIdAndType(
                        subscription.getId(),
                        type
                )
        ).thenReturn(true);

        notificationService.createIfNotExists(
                subscription,
                type
        );

        verify(
                notificationRepository,
                never()
        ).save(any(SubscriptionNotificationEntity.class));
    }


    @Test
    void processPendingNotifications_shouldSendEmailAndMarkAsSent() {

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        SubscriptionNotificationEntity notification =
                SubscriptionNotificationEntity.builder()
                        .id(UUID.randomUUID())
                        .subscription(subscription)
                        .type(
                                SubscriptionNotificationType
                                        .EXPIRING_IN_7_DAYS
                        )
                        .status(NotificationStatus.PENDING)
                        .attempts(0)
                        .build();

        when(
                notificationRepository.findByStatusOrderByCreatedAtAsc(
                        eq(NotificationStatus.PENDING),
                        eq(PageRequest.of(0, 50))
                )
        ).thenReturn(List.of(notification));

        when(
                notificationRepository.claimNotification(
                        any(UUID.class),
                        eq(NotificationStatus.PENDING),
                        eq(NotificationStatus.PROCESSING),
                        any(LocalDateTime.class)
                )
        ).thenReturn(1);

        notificationService.processPendingNotifications();

        verify(emailService).sendSubscriptionExpirationEmail(
                subscription,
                notification.getType()
        );

        verify(notificationRepository).save(
                notification
        );

        assertEquals(
                NotificationStatus.SENT,
                notification.getStatus()
        );

        assertEquals(
                null,
                notification.getLockedAt()
        );

        assertEquals(
                null,
                notification.getLastError()
        );
    }


    @Test
    void processPendingNotifications_shouldNotSendEmailWhenClaimFails() {

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        SubscriptionNotificationEntity notification =
                SubscriptionNotificationEntity.builder()
                        .id(UUID.randomUUID())
                        .subscription(subscription)
                        .type(
                                SubscriptionNotificationType
                                        .EXPIRING_IN_7_DAYS
                        )
                        .status(NotificationStatus.PENDING)
                        .attempts(0)
                        .build();

        when(
                notificationRepository.findByStatusOrderByCreatedAtAsc(
                        eq(NotificationStatus.PENDING),
                        eq(PageRequest.of(0, 50))
                )
        ).thenReturn(List.of(notification));

        when(
                notificationRepository.claimNotification(
                        any(UUID.class),
                        eq(NotificationStatus.PENDING),
                        eq(NotificationStatus.PROCESSING),
                        any(LocalDateTime.class)
                )
        ).thenReturn(0);

        notificationService.processPendingNotifications();

        verify(
                emailService,
                never()
        ).sendSubscriptionExpirationEmail(
                any(AgencySubscriptionEntity.class),
                any(SubscriptionNotificationType.class)
        );

        verify(
                notificationRepository,
                never()
        ).save(notification);
    }


    @Test
    void processPendingNotifications_shouldMarkAsFailedWhenEmailSendingFails() {

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        SubscriptionNotificationEntity notification =
                SubscriptionNotificationEntity.builder()
                        .id(UUID.randomUUID())
                        .subscription(subscription)
                        .type(
                                SubscriptionNotificationType
                                        .EXPIRING_IN_7_DAYS
                        )
                        .status(NotificationStatus.PENDING)
                        .attempts(0)
                        .build();

        RuntimeException exception =
                new RuntimeException("Email service failed");

        when(
                notificationRepository.findByStatusOrderByCreatedAtAsc(
                        eq(NotificationStatus.PENDING),
                        eq(PageRequest.of(0, 50))
                )
        ).thenReturn(List.of(notification));

        when(
                notificationRepository.claimNotification(
                        any(UUID.class),
                        eq(NotificationStatus.PENDING),
                        eq(NotificationStatus.PROCESSING),
                        any(LocalDateTime.class)
                )
        ).thenReturn(1);

        doThrow(exception)
                .when(emailService)
                .sendSubscriptionExpirationEmail(
                        subscription,
                        notification.getType()
                );

        notificationService.processPendingNotifications();

        verify(
                notificationRepository
        ).save(notification);

        assertEquals(
                NotificationStatus.FAILED,
                notification.getStatus()
        );

        assertEquals(
                "Email service failed",
                notification.getLastError()
        );

        assertEquals(
                null,
                notification.getLockedAt()
        );
    }


    @Test
    void processPendingNotifications_shouldProcessMultipleNotifications() {

        AgencySubscriptionEntity subscription1 =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        AgencySubscriptionEntity subscription2 =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        SubscriptionNotificationEntity notification1 =
                SubscriptionNotificationEntity.builder()
                        .id(UUID.randomUUID())
                        .subscription(subscription1)
                        .type(
                                SubscriptionNotificationType
                                        .EXPIRING_IN_7_DAYS
                        )
                        .status(NotificationStatus.PENDING)
                        .attempts(0)
                        .build();

        SubscriptionNotificationEntity notification2 =
                SubscriptionNotificationEntity.builder()
                        .id(UUID.randomUUID())
                        .subscription(subscription2)
                        .type(
                                SubscriptionNotificationType
                                        .EXPIRING_IN_1_DAY
                        )
                        .status(NotificationStatus.PENDING)
                        .attempts(0)
                        .build();

        when(
                notificationRepository.findByStatusOrderByCreatedAtAsc(
                        eq(NotificationStatus.PENDING),
                        eq(PageRequest.of(0, 50))
                )
        ).thenReturn(
                List.of(
                        notification1,
                        notification2
                )
        );

        when(
                notificationRepository.claimNotification(
                        any(UUID.class),
                        eq(NotificationStatus.PENDING),
                        eq(NotificationStatus.PROCESSING),
                        any(LocalDateTime.class)
                )
        ).thenReturn(1);

        notificationService.processPendingNotifications();

        verify(emailService).sendSubscriptionExpirationEmail(
                subscription1,
                notification1.getType()
        );

        verify(emailService).sendSubscriptionExpirationEmail(
                subscription2,
                notification2.getType()
        );

        verify(notificationRepository).save(
                notification1
        );

        verify(notificationRepository).save(
                notification2
        );
    }


    @Test
    void releaseStuckNotifications_shouldCallRepository() {

        when(
                notificationRepository.releaseStuckNotifications(
                        eq(NotificationStatus.PROCESSING),
                        eq(NotificationStatus.PENDING),
                        any(LocalDateTime.class)
                )
        ).thenReturn(3);

        notificationService.releaseStuckNotifications();

        verify(notificationRepository)
                .releaseStuckNotifications(
                        eq(NotificationStatus.PROCESSING),
                        eq(NotificationStatus.PENDING),
                        any(LocalDateTime.class)
                );
    }


    @Test
    void releaseStuckNotifications_shouldHandleZeroReleasedNotifications() {

        when(
                notificationRepository.releaseStuckNotifications(
                        eq(NotificationStatus.PROCESSING),
                        eq(NotificationStatus.PENDING),
                        any(LocalDateTime.class)
                )
        ).thenReturn(0);

        notificationService.releaseStuckNotifications();

        verify(notificationRepository)
                .releaseStuckNotifications(
                        eq(NotificationStatus.PROCESSING),
                        eq(NotificationStatus.PENDING),
                        any(LocalDateTime.class)
                );
    }
}