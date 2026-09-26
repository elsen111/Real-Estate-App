package com.realestate.backend.payment.gateway;

import com.realestate.backend.config.StripeProperties;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.PaymentEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.PaymentProcessingException;
import com.stripe.Stripe;
import com.stripe.exception.ApiConnectionException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class StripePaymentGatewayTest {

    private static final UUID PAYMENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID AGENCY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PLAN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final String WEBHOOK_SECRET = "whsec_test_secret";
    private static final String IDEMPOTENCY_KEY = "idem-key-123";

    private final StripeProperties properties = new StripeProperties(
            "sk_test_123",
            WEBHOOK_SECRET,
            "usd",
            "https://app.test/success",
            "https://app.test/cancel"
    );

    private final StripePaymentGateway gateway = new StripePaymentGateway(properties);

    @Captor
    private ArgumentCaptor<Map<String, Object>> paramsCaptor;
    @Captor
    private ArgumentCaptor<RequestOptions> optionsCaptor;

    private AgencyEntity agency;
    private SubscriptionPlanEntity plan;
    private PaymentEntity payment;

    @BeforeEach
    void setUp() {
        agency = AgencyEntity.builder()
                .id(AGENCY_ID)
                .email("agency@example.com")
                .build();

        plan = SubscriptionPlanEntity.builder()
                .id(PLAN_ID)
                .name("Premium")
                .description("Premium plan")
                .price(new BigDecimal("50.00"))
                .build();

        payment = PaymentEntity.builder()
                .id(PAYMENT_ID)
                .agency(agency)
                .plan(plan)
                .idempotencyKey(IDEMPOTENCY_KEY)
                .amount(new BigDecimal("50.00"))
                .currency("usd")
                .build();
    }

    // ------------------------------------------------------------------
    // createCheckout
    // ------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void createCheckout_shouldSendExpectedParamsToStripeAndMapResult() throws Exception {
        Session session = new Session();
        session.setId("cs_test_123");
        session.setUrl("https://checkout.stripe.com/c/pay/cs_test_123");
        session.setPaymentIntent("pi_test_123");

        try (MockedStatic<Session> stripeSession = mockStatic(Session.class)) {
            stripeSession.when(() -> Session.create(anyMap(), any(RequestOptions.class)))
                    .thenReturn(session);

            PaymentCheckoutResult result = gateway.createCheckout(payment, agency, plan);

            assertEquals("cs_test_123", result.checkoutSessionId());
            assertEquals("pi_test_123", result.paymentIntentId());
            assertEquals("https://checkout.stripe.com/c/pay/cs_test_123", result.checkoutUrl());

            stripeSession.verify(() -> Session.create(paramsCaptor.capture(), optionsCaptor.capture()));
        }

        Map<String, Object> params = paramsCaptor.getValue();
        assertEquals("payment", params.get("mode"));
        assertEquals("agency@example.com", params.get("customer_email"));
        assertEquals(PAYMENT_ID.toString(), params.get("client_reference_id"));
        assertEquals("https://app.test/success", params.get("success_url"));
        assertEquals("https://app.test/cancel", params.get("cancel_url"));

        Map<String, Object> metadata = (Map<String, Object>) params.get("metadata");
        assertEquals(PAYMENT_ID.toString(), metadata.get("paymentId"));
        assertEquals(AGENCY_ID.toString(), metadata.get("agencyId"));
        assertEquals(PLAN_ID.toString(), metadata.get("planId"));

        Map<String, Object> intentData = (Map<String, Object>) params.get("payment_intent_data");
        assertEquals(metadata, intentData.get("metadata"));

        Map<String, Object> lineItem = ((List<Map<String, Object>>) params.get("line_items")).get(0);
        assertEquals(1, lineItem.get("quantity"));

        Map<String, Object> priceData = (Map<String, Object>) lineItem.get("price_data");
        assertEquals("usd", priceData.get("currency"));
        assertEquals(5000L, priceData.get("unit_amount"));

        Map<String, Object> productData = (Map<String, Object>) priceData.get("product_data");
        assertEquals("Premium", productData.get("name"));
        assertEquals("Premium plan", productData.get("description"));

        assertEquals(AGENCY_ID + ":" + IDEMPOTENCY_KEY, optionsCaptor.getValue().getIdempotencyKey());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createCheckout_shouldOmitProductDescriptionWhenPlanHasNone() throws Exception {
        plan.setDescription(null);

        try (MockedStatic<Session> stripeSession = mockStatic(Session.class)) {
            stripeSession.when(() -> Session.create(anyMap(), any(RequestOptions.class)))
                    .thenReturn(new Session());

            gateway.createCheckout(payment, agency, plan);

            stripeSession.verify(() -> Session.create(paramsCaptor.capture(), optionsCaptor.capture()));
        }

        Map<String, Object> lineItem =
                ((List<Map<String, Object>>) paramsCaptor.getValue().get("line_items")).get(0);
        Map<String, Object> priceData = (Map<String, Object>) lineItem.get("price_data");
        Map<String, Object> productData = (Map<String, Object>) priceData.get("product_data");

        assertFalse(productData.containsKey("description"));
    }

    @Test
    void createCheckout_shouldWrapStripeErrors() {
        try (MockedStatic<Session> stripeSession = mockStatic(Session.class)) {
            stripeSession.when(() -> Session.create(anyMap(), any(RequestOptions.class)))
                    .thenThrow(new ApiConnectionException("Stripe unreachable"));

            PaymentProcessingException exception = assertThrows(
                    PaymentProcessingException.class,
                    () -> gateway.createCheckout(payment, agency, plan)
            );

            assertEquals("Failed to create Stripe checkout session.", exception.getMessage());
        }
    }

    @Test
    void createCheckout_shouldRejectAmountWithMoreThanTwoDecimals() {
        payment.setAmount(new BigDecimal("50.001"));

        assertThrows(
                PaymentProcessingException.class,
                () -> gateway.createCheckout(payment, agency, plan)
        );
    }

    // ------------------------------------------------------------------
    // parseWebhook
    // ------------------------------------------------------------------

    @Test
    void parseWebhook_shouldParseCompletedCheckoutAsSucceeded() throws Exception {
        String payload = event("checkout.session.completed", sessionJson(PAYMENT_ID.toString()));

        PaymentWebhookData data = gateway.parseWebhook(payload, signatureFor(payload));

        assertEquals("evt_123", data.eventId());
        assertEquals("checkout.session.completed", data.eventType());
        assertEquals(PaymentWebhookType.PAYMENT_SUCCEEDED, data.type());
        assertEquals(PAYMENT_ID, data.paymentId());
        assertEquals("cs_test_123", data.checkoutSessionId());
        assertEquals("pi_test_123", data.paymentIntentId());
        assertEquals(5000L, data.amountMinor());
        assertEquals("usd", data.currency());
        assertNull(data.failureReason());
    }

    @Test
    void parseWebhook_shouldParseAsyncPaymentSucceededAsSucceeded() throws Exception {
        String payload = event("checkout.session.async_payment_succeeded", sessionJson(PAYMENT_ID.toString()));

        PaymentWebhookData data = gateway.parseWebhook(payload, signatureFor(payload));

        assertEquals(PaymentWebhookType.PAYMENT_SUCCEEDED, data.type());
    }

    @Test
    void parseWebhook_shouldParseFailedPaymentIntentAsFailed() throws Exception {
        String payload = event(
                "payment_intent.payment_failed", """
                        {"id":"pi_test_123","object":"payment_intent","amount":5000,"currency":"usd",
                         "metadata":{"paymentId":"%s"}}
                        """.formatted(PAYMENT_ID)
        );

        PaymentWebhookData data = gateway.parseWebhook(payload, signatureFor(payload));

        assertEquals(PaymentWebhookType.PAYMENT_FAILED, data.type());
        assertEquals(PAYMENT_ID, data.paymentId());
        assertEquals("pi_test_123", data.paymentIntentId());
        assertEquals(5000L, data.amountMinor());
        assertEquals("Payment was declined or failed.", data.failureReason());
    }

    @Test
    void parseWebhook_shouldParseExpiredCheckoutAsCanceled() throws Exception {
        String payload = event("checkout.session.expired", sessionJson(PAYMENT_ID.toString()));

        PaymentWebhookData data = gateway.parseWebhook(payload, signatureFor(payload));

        assertEquals(PaymentWebhookType.PAYMENT_CANCELED, data.type());
        assertEquals(PAYMENT_ID, data.paymentId());
        assertEquals("Payment was not completed.", data.failureReason());
    }

    @Test
    void parseWebhook_shouldMarkUnknownEventsAsUnsupported() throws Exception {
        String payload = event(
                "customer.created", """
                        {"id":"cus_123","object":"customer"}
                        """
        );

        PaymentWebhookData data = gateway.parseWebhook(payload, signatureFor(payload));

        assertEquals(PaymentWebhookType.UNSUPPORTED, data.type());
        assertEquals("evt_123", data.eventId());
        assertNull(data.paymentId());
    }

    @Test
    void parseWebhook_shouldRejectInvalidSignature() {
        String payload = event("checkout.session.completed", sessionJson(PAYMENT_ID.toString()));

        assertThrows(
                BadRequestException.class,
                () -> gateway.parseWebhook(payload, "t=" + Instant.now().getEpochSecond() + ",v1=deadbeef")
        );
    }

    @Test
    void parseWebhook_shouldRejectMalformedPayload() throws Exception {
        String payload = "{ not json";

        assertThrows(
                BadRequestException.class,
                () -> gateway.parseWebhook(payload, signatureFor(payload))
        );
    }

    @Test
    void parseWebhook_shouldRejectEventFromIncompatibleApiVersion() throws Exception {
        // Old endpoint API version -> Stripe SDK refuses to deserialize the data object
        String payload = """
                {"id":"evt_123","object":"event","api_version":"2000-01-01","type":"checkout.session.completed",
                 "data":{"object":%s}}
                """.formatted(sessionJson(PAYMENT_ID.toString()));

        assertThrows(
                BadRequestException.class,
                () -> gateway.parseWebhook(payload, signatureFor(payload))
        );
    }

    @Test
    void parseWebhook_shouldRejectMissingPaymentIdMetadata() throws Exception {
        String payload = event(
                "checkout.session.completed", """
                        {"id":"cs_test_123","object":"checkout.session","payment_intent":"pi_test_123",
                         "amount_total":5000,"currency":"usd","payment_status":"paid","metadata":{}}
                        """
        );

        assertThrows(
                BadRequestException.class,
                () -> gateway.parseWebhook(payload, signatureFor(payload))
        );
    }

    @Test
    void parseWebhook_shouldRejectInvalidPaymentIdMetadata() throws Exception {
        String payload = event("checkout.session.completed", sessionJson("not-a-uuid"));

        assertThrows(
                BadRequestException.class,
                () -> gateway.parseWebhook(payload, signatureFor(payload))
        );
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private String sessionJson(String paymentId) {
        return """
                {"id":"cs_test_123","object":"checkout.session","payment_intent":"pi_test_123",
                 "amount_total":5000,"currency":"usd","payment_status":"paid",
                 "metadata":{"paymentId":"%s"}}
                """.formatted(paymentId);
    }

    private String event(String type, String objectJson) {
        return """
                {"id":"evt_123","object":"event","api_version":"%s","created":1700000000,
                 "livemode":false,"type":"%s","data":{"object":%s}}
                """.formatted(Stripe.API_VERSION, type, objectJson);
    }

    private String signatureFor(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        String signature = Webhook.Util.computeHmacSha256(WEBHOOK_SECRET, timestamp + "." + payload);
        return "t=" + timestamp + ",v1=" + signature;
    }
}