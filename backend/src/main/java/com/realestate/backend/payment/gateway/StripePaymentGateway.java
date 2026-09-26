package com.realestate.backend.payment.gateway;

import com.google.gson.JsonSyntaxException;
import com.realestate.backend.config.StripeProperties;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.PaymentEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.PaymentProcessingException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway {

    private final StripeProperties properties;

    @Override
    public PaymentCheckoutResult createCheckout(
            PaymentEntity payment,
            AgencyEntity agency,
            SubscriptionPlanEntity plan
    ) {

        long amountMinor = toMinorUnits(payment.getAmount());

        Map<String, Object> metadata = Map.of(
                "paymentId", payment.getId().toString(),
                "agencyId", agency.getId().toString(),
                "planId", plan.getId().toString()
        );

        Map<String, Object> priceData = new HashMap<>();
        priceData.put(
                "currency",
                properties.currency().toLowerCase()
        );
        priceData.put(
                "unit_amount",
                amountMinor
        );

        Map<String, Object> productData = new HashMap<>();
        productData.put("name", plan.getName());

        if (plan.getDescription() != null) {
            productData.put(
                    "description",
                    plan.getDescription()
            );
        }

        priceData.put("product_data", productData);

        Map<String, Object> lineItem = Map.of(
                "price_data",
                priceData,
                "quantity",
                1
        );

        Map<String, Object> paymentIntentData = Map.of(
                "metadata",
                metadata
        );

        Map<String, Object> params = new HashMap<>();
        params.put("mode", "payment");
        params.put("line_items", List.of(lineItem));
        params.put("customer_email", agency.getEmail());
        params.put(
                "client_reference_id",
                payment.getId().toString()
        );
        params.put("metadata", metadata);
        params.put(
                "payment_intent_data",
                paymentIntentData
        );
        params.put(
                "success_url",
                properties.successUrl()
        );
        params.put(
                "cancel_url",
                properties.cancelUrl()
        );

        String stripeIdempotencyKey =
                agency.getId() + ":" + payment.getIdempotencyKey();

        RequestOptions requestOptions =
                RequestOptions.builder()
                        .setApiKey(properties.secretKey())
                        .setIdempotencyKey(stripeIdempotencyKey)
                        .build();

        try {

            Session session =
                    Session.create(
                            params,
                            requestOptions
                    );

            return new PaymentCheckoutResult(
                    session.getId(),
                    session.getPaymentIntent(),
                    session.getUrl()
            );

        } catch (Exception e) {

            throw new PaymentProcessingException(
                    "Failed to create Stripe checkout session.",
                    e
            );
        }
    }

    @Override
    public PaymentWebhookData parseWebhook(
            String payload,
            String signature
    ) {

        Event event;

        try {

            event = Webhook.constructEvent(
                    payload,
                    signature,
                    properties.webhookSecret()
            );

        } catch (SignatureVerificationException e) {

            log.error("Stripe signature verification failed.", e);

            throw new BadRequestException(
                    "Invalid payment webhook signature."
            );

        } catch (JsonSyntaxException e) {

            throw new BadRequestException(
                    "Invalid payment webhook payload."
            );
        }

        StripeObject stripeObject =
                event.getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);

        if (stripeObject == null) {
            throw new BadRequestException(
                    "Payment webhook data is missing."
            );
        }

        return switch (event.getType()) {

            case "checkout.session.completed",
                 "checkout.session.async_payment_succeeded" -> parseSuccessfulCheckout(event, stripeObject);

            case "payment_intent.payment_failed" -> parseFailedPaymentIntent(event, stripeObject);

            case "checkout.session.expired",
                 "checkout.session.async_payment_failed" -> parseCanceledCheckout(event, stripeObject);

            default -> new PaymentWebhookData(
                    event.getId(),
                    event.getType(),
                    PaymentWebhookType.UNSUPPORTED,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        };
    }

    private PaymentWebhookData parseSuccessfulCheckout(
            Event event,
            StripeObject stripeObject
    ) {

        Session session = (Session) stripeObject;

        UUID paymentId =
                parsePaymentId(
                        session.getMetadata().get("paymentId")
                );

        return new PaymentWebhookData(
                event.getId(),
                event.getType(),
                PaymentWebhookType.PAYMENT_SUCCEEDED,
                paymentId,
                session.getId(),
                session.getPaymentIntent(),
                session.getAmountTotal(),
                session.getCurrency(),
                null
        );
    }

    private PaymentWebhookData parseFailedPaymentIntent(
            Event event,
            StripeObject stripeObject
    ) {

        PaymentIntent paymentIntent =
                (PaymentIntent) stripeObject;

        UUID paymentId =
                parsePaymentId(
                        paymentIntent.getMetadata()
                                .get("paymentId")
                );

        return new PaymentWebhookData(
                event.getId(),
                event.getType(),
                PaymentWebhookType.PAYMENT_FAILED,
                paymentId,
                null,
                paymentIntent.getId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency(),
                "Payment was declined or failed."
        );
    }

    private PaymentWebhookData parseCanceledCheckout(
            Event event,
            StripeObject stripeObject
    ) {

        Session session = (Session) stripeObject;

        UUID paymentId =
                parsePaymentId(
                        session.getMetadata().get("paymentId")
                );

        return new PaymentWebhookData(
                event.getId(),
                event.getType(),
                PaymentWebhookType.PAYMENT_CANCELED,
                paymentId,
                session.getId(),
                session.getPaymentIntent(),
                session.getAmountTotal(),
                session.getCurrency(),
                "Payment was not completed."
        );
    }

    private UUID parsePaymentId(String value) {

        if (value == null || value.isBlank()) {
            throw new BadRequestException(
                    "Payment identifier is missing from webhook."
            );
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid payment identifier in webhook."
            );
        }
    }

    private long toMinorUnits(BigDecimal amount) {

        try {
            return amount
                    .movePointRight(2)
                    .longValueExact();

        } catch (ArithmeticException e) {

            throw new PaymentProcessingException(
                    "Payment amount must have at most two decimal places.",
                    e
            );
        }
    }
}