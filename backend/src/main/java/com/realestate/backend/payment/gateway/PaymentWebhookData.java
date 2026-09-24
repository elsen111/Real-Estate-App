package com.realestate.backend.payment.gateway;

import java.util.UUID;

public record PaymentWebhookData(
        String eventId,
        String eventType,
        PaymentWebhookType type,
        UUID paymentId,
        String checkoutSessionId,
        String paymentIntentId,
        Long amountMinor,
        String currency,
        String failureReason
) {
}