package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.PaymentCheckoutResponse;
import com.realestate.backend.dto.response.PaymentResponse;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(
        name = "Payments",
        description = """
                APIs for managing subscription payments, including creating checkout sessions,
                retrieving payment details, and processing payment provider webhook events.
                """
)
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(
            summary = "Create a subscription payment checkout",
            description = """
                    Creates a checkout session for the specified subscription plan using the
                    configured payment provider.
                    Requires an agency owner account and a valid subscription plan ID.
                    An Idempotency-Key header is required to prevent duplicate checkout operations
                    when the same request is retried.
                    """
    )
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
    @Operation(
            summary = "Get payment details by ID",
            description = """
                    Retrieves payment details using the payment's unique identifier.
                    The authenticated agency owner can access payments associated with their account.
                    Requires the AGENCY_OWNER role.
                    """
    )
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
    @Operation(
            summary = "Get payment details by checkout session",
            description = """
                    Retrieves payment details associated with a specific payment provider checkout session.
                    The authenticated agency owner can access payments associated with their account.
                    Requires the AGENCY_OWNER role.
                    """
    )
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
    @Operation(
            summary = "Process payment provider webhook",
            description = """
                    Receives and processes webhook events sent by Stripe to synchronize payment
                    information and subscription-related state.
                    Validates the webhook signature using the Stripe-Signature header before
                    processing the event.
                    This endpoint is intended for Stripe server-to-server communication
                    and does not require user authentication.
                    """
    )
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