package com.realestate.backend.service;

import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.enums.SubscriptionNotificationType;

public interface SubscriptionNotificationService {

    void createIfNotExists(
            AgencySubscriptionEntity subscription,
            SubscriptionNotificationType type
    );

    void processPendingNotifications();

    void releaseStuckNotifications();
}