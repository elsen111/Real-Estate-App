package com.realestate.backend.service;

import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.enums.SubscriptionNotificationType;

public interface EmailService {

    void sendPasswordResetOtp(String toEmail, String otp);

    void sendSubscriptionExpirationEmail( AgencySubscriptionEntity subscription, SubscriptionNotificationType type);

}