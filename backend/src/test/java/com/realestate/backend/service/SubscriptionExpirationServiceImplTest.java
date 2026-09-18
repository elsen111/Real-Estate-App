package com.realestate.backend.service;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.enums.SubscriptionNotificationType;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.repository.AgencySubscriptionRepository;
import com.realestate.backend.service.impl.SubscriptionExpirationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionExpirationServiceImplTest {

    @Mock
    private AgencySubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionNotificationService notificationService;

    @InjectMocks
    private SubscriptionExpirationServiceImpl expirationService;

    @Test
    void processSubscriptions_shouldExpireExpiredSubscriptions() {

        LocalDate today = LocalDate.now();

        AgencyEntity agency =
                AgencyEntity.builder()
                        .id(UUID.randomUUID())
                        .build();

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .status(SubscriptionStatus.ACTIVE)
                        .endDate(today.minusDays(1))
                        .build();

        when(
                subscriptionRepository.findByStatusAndEndDateBefore(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of(subscription));

        when(
                subscriptionRepository.findByStatusAndEndDate(
                        eq(
                                SubscriptionStatus.ACTIVE
                        ),
                        eq(today.plusDays(7)),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        when(
                subscriptionRepository.findByStatusAndEndDate(
                        eq(
                                SubscriptionStatus.ACTIVE
                        ),
                        eq(today.plusDays(1)),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        expirationService.processSubscriptions();

        verify(subscriptionRepository).save(subscription);

        verify(notificationService).createIfNotExists(
                subscription,
                SubscriptionNotificationType.EXPIRED
        );
    }

    @Test
    void processSubscriptions_shouldCreateSevenDayReminder() {

        LocalDate today = LocalDate.now();

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .status(SubscriptionStatus.ACTIVE)
                        .endDate(today.plusDays(7))
                        .build();

        when(
                subscriptionRepository.findByStatusAndEndDateBefore(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        when(
                subscriptionRepository.findByStatusAndEndDate(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today.plusDays(7)),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of(subscription));

        when(
                subscriptionRepository.findByStatusAndEndDate(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today.plusDays(1)),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        expirationService.processSubscriptions();

        verify(notificationService).createIfNotExists(
                subscription,
                SubscriptionNotificationType.EXPIRING_IN_7_DAYS
        );

        verify(subscriptionRepository, never())
                .save(subscription);
    }

    @Test
    void processSubscriptions_shouldCreateOneDayReminder() {

        LocalDate today = LocalDate.now();

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .id(UUID.randomUUID())
                        .status(SubscriptionStatus.ACTIVE)
                        .endDate(today.plusDays(1))
                        .build();

        when(
                subscriptionRepository.findByStatusAndEndDateBefore(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        when(
                subscriptionRepository.findByStatusAndEndDate(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today.plusDays(7)),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        when(
                subscriptionRepository.findByStatusAndEndDate(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today.plusDays(1)),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of(subscription));

        expirationService.processSubscriptions();

        verify(notificationService).createIfNotExists(
                subscription,
                SubscriptionNotificationType.EXPIRING_IN_1_DAY
        );

        verify(subscriptionRepository, never())
                .save(subscription);
    }

    @Test
    void processSubscriptions_shouldDoNothingWhenThereAreNoSubscriptions() {

        LocalDate today = LocalDate.now();

        when(
                subscriptionRepository.findByStatusAndEndDateBefore(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        when(
                subscriptionRepository.findByStatusAndEndDate(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today.plusDays(7)),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        when(
                subscriptionRepository.findByStatusAndEndDate(
                        eq(SubscriptionStatus.ACTIVE),
                        eq(today.plusDays(1)),
                        eq(PageRequest.of(0, 100))
                )
        ).thenReturn(List.of());

        expirationService.processSubscriptions();

        verify(subscriptionRepository, never())
                .save(any());

        verify(notificationService, never())
                .createIfNotExists(any(), any());
    }
}