package com.realestate.backend.service;

import com.realestate.backend.dto.response.PaymentCheckoutResponse;
import com.realestate.backend.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentCheckoutResponse createCheckout(
            UUID userId,
            UUID planId,
            String idempotencyKey
    );

    void handleWebhook(
            String payload,
            String signature
    );

    PaymentResponse getPayment(
            UUID userId,
            UUID paymentId
    );

    PaymentResponse getPaymentBySession(
            UUID userId,
            String checkoutSessionId
    );
}