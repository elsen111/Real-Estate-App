package com.realestate.backend.scheduler;

import com.realestate.backend.service.SubscriptionExpirationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionExpirationSchedulerTest {

    @Mock
    private SubscriptionExpirationService subscriptionExpirationService;

    @InjectMocks
    private SubscriptionExpirationScheduler scheduler;

    @Test
    void processSubscriptions_shouldCallExpirationService() {

        scheduler.processSubscriptions();

        verify(
                subscriptionExpirationService,
                times(1)
        ).processSubscriptions();
    }
}