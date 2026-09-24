package com.realestate.backend.payment.gateway;

public enum PaymentWebhookType {
    PAYMENT_SUCCEEDED,
    PAYMENT_FAILED,
    PAYMENT_CANCELED,
    UNSUPPORTED
}