package com.realestate.backend.payment.gateway;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.PaymentEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;

public interface PaymentGateway {

    PaymentCheckoutResult createCheckout(
            PaymentEntity payment,
            AgencyEntity agency,
            SubscriptionPlanEntity plan
    );

    PaymentWebhookData parseWebhook(
            String payload,
            String signature
    );

}