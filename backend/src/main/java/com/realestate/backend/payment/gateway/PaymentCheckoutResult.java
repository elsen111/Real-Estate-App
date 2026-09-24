package com.realestate.backend.payment.gateway;

public record PaymentCheckoutResult(
        String checkoutSessionId,
        String paymentIntentId,
        String checkoutUrl
) {
}