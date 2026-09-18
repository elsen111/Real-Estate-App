package com.realestate.backend.scheduler;

import com.realestate.backend.service.SubscriptionNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionEmailSchedulerTest {

    @Mock
    private SubscriptionNotificationService subscriptionNotificationService;

    @InjectMocks
    private SubscriptionEmailScheduler scheduler;

    @Test
    void processEmails_shouldCallNotificationService() {

        scheduler.processEmails();

        verify(
                subscriptionNotificationService,
                times(1)
        ).processPendingNotifications();
    }
}