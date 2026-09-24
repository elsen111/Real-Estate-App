package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.PaymentCheckoutResponse;
import com.realestate.backend.dto.response.PaymentResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(summary = "Create payment checkout for a subscription plan")
    public ResponseEntity<ApiResponse<PaymentCheckoutResponse>> createCheckout(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam UUID planId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {

        PaymentCheckoutResponse response =
                paymentService.createCheckout(
                        currentUser.getId(),
                        planId,
                        idempotencyKey
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment checkout created successfully.",
                        response
                )
        );
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(summary = "Get payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable UUID paymentId
    ) {

        PaymentResponse response =
                paymentService.getPayment(
                        currentUser.getId(),
                        paymentId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment fetched successfully.",
                        response
                )
        );
    }

    @GetMapping("/session/{checkoutSessionId}")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(summary = "Get payment by checkout session")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentBySession(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String checkoutSessionId
    ) {

        PaymentResponse response =
                paymentService.getPaymentBySession(
                        currentUser.getId(),
                        checkoutSessionId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment fetched successfully.",
                        response
                )
        );
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {

        paymentService.handleWebhook(
                payload,
                signature
        );

        return ResponseEntity.ok().build();
    }
}