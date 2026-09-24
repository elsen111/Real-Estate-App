package com.realestate.backend.service;

import com.realestate.backend.dto.response.PaymentCheckoutResponse;
import com.realestate.backend.dto.response.PaymentResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.PaymentEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.PaymentStatus;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.PaymentProcessingException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.payment.gateway.PaymentCheckoutResult;
import com.realestate.backend.payment.gateway.PaymentGateway;
import com.realestate.backend.payment.gateway.PaymentWebhookData;
import com.realestate.backend.payment.gateway.PaymentWebhookType;
import com.realestate.backend.repository.AgencySubscriptionRepository;
import com.realestate.backend.repository.PaymentRepository;
import com.realestate.backend.repository.PaymentWebhookEventRepository;
import com.realestate.backend.repository.SubscriptionPlanRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID AGENCY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PLAN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_PLAN_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PAYMENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID SUBSCRIPTION_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static final String IDEMPOTENCY_KEY = "idem-key-123";
    private static final String CURRENCY = "usd";
    private static final String CHECKOUT_URL = "https://checkout.stripe.com/test";
    private static final String CHECKOUT_SESSION_ID = "cs_test_123";
    private static final String PAYMENT_INTENT_ID = "pi_test_123";
    private static final String PAYLOAD = "payload";
    private static final String SIGNATURE = "signature";

    @Mock private UserRepository userRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private AgencySubscriptionRepository subscriptionRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentWebhookEventRepository webhookEventRepository;
    @Mock private PaymentGateway paymentGateway;
    @Mock private EmailService emailService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private AgencyEntity agency;
    private SubscriptionPlanEntity plan;
    private UserEntity user;
    private PaymentEntity payment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "currency", CURRENCY);

        agency = AgencyEntity.builder()
                .id(AGENCY_ID)
                .email("agency@example.com")
                .isDeleted(false)
                .build();

        plan = SubscriptionPlanEntity.builder()
                .id(PLAN_ID)
                .name("Premium")
                .price(new BigDecimal("50.00"))
                .durationDays(30)
                .build();

        user = UserEntity.builder()
                .id(USER_ID)
                .agency(agency)
                .build();

        payment = PaymentEntity.builder()
                .id(PAYMENT_ID)
                .agency(agency)
                .plan(plan)
                .idempotencyKey(IDEMPOTENCY_KEY)
                .amount(new BigDecimal("50.00"))
                .currency(CURRENCY)
                .status(PaymentStatus.PENDING)
                .checkoutUrl(CHECKOUT_URL)
                .build();
    }

    // ------------------------------------------------------------------
    // createCheckout
    // ------------------------------------------------------------------

    @Test
    void createCheckout_shouldCreatePendingPaymentAndReturnCheckoutUrl() {
        givenAuthenticatedUser();
        when(subscriptionPlanRepository.findByIdAndActiveTrueAndDeletedFalse(PLAN_ID))
                .thenReturn(Optional.of(plan));
        when(paymentRepository.saveAndFlush(any(PaymentEntity.class)))
                .thenAnswer(invocation -> {
                    PaymentEntity saved = invocation.getArgument(0);
                    saved.setId(PAYMENT_ID);
                    return saved;
                });
        when(paymentGateway.createCheckout(any(PaymentEntity.class), eq(agency), eq(plan)))
                .thenReturn(new PaymentCheckoutResult(CHECKOUT_SESSION_ID, PAYMENT_INTENT_ID, CHECKOUT_URL));

        PaymentCheckoutResponse response =
                paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY);

        assertEquals(PAYMENT_ID, response.getPaymentId());
        assertEquals(CHECKOUT_URL, response.getCheckoutUrl());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals(CURRENCY, response.getCurrency());
        assertEquals(PaymentStatus.PENDING, response.getStatus());

        ArgumentCaptor<PaymentEntity> captor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).saveAndFlush(captor.capture());
        PaymentEntity saved = captor.getValue();

        assertSame(agency, saved.getAgency());
        assertSame(plan, saved.getPlan());
        assertEquals(IDEMPOTENCY_KEY, saved.getIdempotencyKey());
        assertEquals(new BigDecimal("50.00"), saved.getAmount());
        assertEquals(CHECKOUT_SESSION_ID, saved.getProviderCheckoutSessionId());
        assertEquals(PAYMENT_INTENT_ID, saved.getProviderPaymentIntentId());
        assertEquals(CHECKOUT_URL, saved.getCheckoutUrl());

        verify(paymentGateway).createCheckout(saved, agency, plan);
        verify(paymentRepository).save(saved);
    }

    @Test
    void createCheckout_shouldReturnExistingPaymentForSameIdempotencyKey() {
        givenAuthenticatedUser();
        when(paymentRepository.findByAgencyIdAndIdempotencyKey(AGENCY_ID, IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(payment));

        PaymentCheckoutResponse response =
                paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY);

        assertEquals(PAYMENT_ID, response.getPaymentId());
        assertEquals(CHECKOUT_URL, response.getCheckoutUrl());
        assertEquals(PaymentStatus.PENDING, response.getStatus());

        verifyNoInteractions(subscriptionPlanRepository, paymentGateway);
    }

    @Test
    void createCheckout_shouldThrowConflictWhenIdempotencyKeyUsedForAnotherPlan() {
        givenAuthenticatedUser();
        when(paymentRepository.findByAgencyIdAndIdempotencyKey(AGENCY_ID, IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(payment));

        assertThrows(ConflictException.class,
                () -> paymentService.createCheckout(USER_ID, OTHER_PLAN_ID, IDEMPOTENCY_KEY));

        verifyNoInteractions(subscriptionPlanRepository, paymentGateway);
    }

    @Test
    void createCheckout_shouldThrowConflictWhenSamePlanIsAlreadyActive() {
        givenAuthenticatedUser();
        when(subscriptionPlanRepository.findByIdAndActiveTrueAndDeletedFalse(PLAN_ID))
                .thenReturn(Optional.of(plan));

        AgencySubscriptionEntity active = AgencySubscriptionEntity.builder()
                .id(SUBSCRIPTION_ID)
                .agency(agency)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionRepository.findFirstByAgencyIdAndStatusOrderByEndDateDesc(
                AGENCY_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(active));

        assertThrows(ConflictException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY));

        verify(paymentRepository, never()).saveAndFlush(any());
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void createCheckout_shouldThrowConflictWhenAgencyHasPendingPayment() {
        givenAuthenticatedUser();
        when(subscriptionPlanRepository.findByIdAndActiveTrueAndDeletedFalse(PLAN_ID))
                .thenReturn(Optional.of(plan));
        when(paymentRepository.existsByAgencyIdAndStatus(AGENCY_ID, PaymentStatus.PENDING))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY));

        verify(paymentRepository, never()).saveAndFlush(any());
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void createCheckout_shouldThrowWhenPlanNotFound() {
        givenAuthenticatedUser();
        when(subscriptionPlanRepository.findByIdAndActiveTrueAndDeletedFalse(PLAN_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY));

        verify(paymentRepository, never()).saveAndFlush(any());
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void createCheckout_shouldMarkPaymentFailedWhenGatewayFails() {
        givenAuthenticatedUser();
        when(subscriptionPlanRepository.findByIdAndActiveTrueAndDeletedFalse(PLAN_ID))
                .thenReturn(Optional.of(plan));
        when(paymentGateway.createCheckout(any(PaymentEntity.class), eq(agency), eq(plan)))
                .thenThrow(new PaymentProcessingException("Stripe checkout failed."));

        PaymentProcessingException exception = assertThrows(PaymentProcessingException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY));

        assertEquals("Stripe checkout failed.", exception.getMessage());

        ArgumentCaptor<PaymentEntity> captor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(captor.capture());
        PaymentEntity saved = captor.getValue();

        assertEquals(PaymentStatus.FAILED, saved.getStatus());
        assertEquals("Stripe checkout failed.", saved.getFailureReason());
        verify(emailService).sendPaymentFailureEmail(saved);
    }

    @Test
    void createCheckout_shouldReturnExistingPaymentAfterConcurrentInsert() {
        givenAuthenticatedUser();
        when(subscriptionPlanRepository.findByIdAndActiveTrueAndDeletedFalse(PLAN_ID))
                .thenReturn(Optional.of(plan));

        // first lookup: nothing yet; second lookup (after the unique-key clash): the winner
        when(paymentRepository.findByAgencyIdAndIdempotencyKey(AGENCY_ID, IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty(), Optional.of(payment));
        when(paymentRepository.saveAndFlush(any(PaymentEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        PaymentCheckoutResponse response =
                paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY);

        assertEquals(PAYMENT_ID, response.getPaymentId());
        assertEquals(CHECKOUT_URL, response.getCheckoutUrl());
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void createCheckout_shouldThrowWhenUserNotFound() {
        when(userRepository.findWithAgencyById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY));

        verifyNoInteractions(subscriptionPlanRepository, paymentRepository, paymentGateway, emailService);
    }

    @Test
    void createCheckout_shouldThrowWhenUserHasNoAgency() {
        user.setAgency(null);
        givenAuthenticatedUser();

        assertThrows(BadRequestException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY));

        verifyNoInteractions(subscriptionPlanRepository, paymentRepository, paymentGateway, emailService);
    }

    @Test
    void createCheckout_shouldThrowWhenAgencyIsDeleted() {
        agency.setIsDeleted(true);
        givenAuthenticatedUser();

        assertThrows(BadRequestException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY));

        verifyNoInteractions(subscriptionPlanRepository, paymentRepository, paymentGateway, emailService);
    }

    @Test
    void createCheckout_shouldThrowWhenAgencyEmailIsMissing() {
        agency.setEmail(null);
        givenAuthenticatedUser();

        assertThrows(BadRequestException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, IDEMPOTENCY_KEY));

        verifyNoInteractions(subscriptionPlanRepository, paymentRepository, paymentGateway, emailService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createCheckout_shouldThrowWhenIdempotencyKeyIsMissingOrBlank(String key) {
        assertThrows(BadRequestException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, key));

        verifyNoInteractions(userRepository, subscriptionPlanRepository, paymentRepository, paymentGateway);
    }

    @Test
    void createCheckout_shouldThrowWhenIdempotencyKeyIsTooLong() {
        String longKey = "a".repeat(256);

        assertThrows(BadRequestException.class,
                () -> paymentService.createCheckout(USER_ID, PLAN_ID, longKey));

        verifyNoInteractions(userRepository, subscriptionPlanRepository, paymentRepository, paymentGateway);
    }

    // ------------------------------------------------------------------
    // getPayment
    // ------------------------------------------------------------------

    @Test
    void getPayment_shouldReturnPaymentResponse() {
        givenAuthenticatedUser();
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setSubscription(AgencySubscriptionEntity.builder().id(SUBSCRIPTION_ID).build());
        when(paymentRepository.findByIdAndAgencyId(PAYMENT_ID, AGENCY_ID))
                .thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPayment(USER_ID, PAYMENT_ID);

        assertEquals(PAYMENT_ID, response.getId());
        assertEquals(AGENCY_ID, response.getAgencyId());
        assertEquals(PLAN_ID, response.getPlanId());
        assertEquals("Premium", response.getPlanName());
        assertEquals(SUBSCRIPTION_ID, response.getSubscriptionId());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals(CURRENCY, response.getCurrency());
        assertEquals(PaymentStatus.SUCCEEDED, response.getStatus());
        assertNull(response.getFailureReason());
    }

    @Test
    void getPayment_shouldReturnResponseWithoutSubscription() {
        givenAuthenticatedUser();
        when(paymentRepository.findByIdAndAgencyId(PAYMENT_ID, AGENCY_ID))
                .thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPayment(USER_ID, PAYMENT_ID);

        assertNull(response.getSubscriptionId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
    }

    @Test
    void getPayment_shouldThrowWhenPaymentNotFound() {
        givenAuthenticatedUser();
        when(paymentRepository.findByIdAndAgencyId(PAYMENT_ID, AGENCY_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPayment(USER_ID, PAYMENT_ID));
    }

    @Test
    void getPayment_shouldThrowWhenUserNotFound() {
        when(userRepository.findWithAgencyById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPayment(USER_ID, PAYMENT_ID));

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void getPayment_shouldThrowWhenUserHasNoAgency() {
        user.setAgency(null);
        givenAuthenticatedUser();

        assertThrows(BadRequestException.class,
                () -> paymentService.getPayment(USER_ID, PAYMENT_ID));

        verifyNoInteractions(paymentRepository);
    }

    // ------------------------------------------------------------------
    // getPaymentBySession
    // ------------------------------------------------------------------

    @Test
    void getPaymentBySession_shouldReturnPayment() {
        givenAuthenticatedUser();
        when(paymentRepository.findByProviderCheckoutSessionId(CHECKOUT_SESSION_ID))
                .thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentBySession(USER_ID, CHECKOUT_SESSION_ID);

        assertEquals(PAYMENT_ID, response.getId());
        assertEquals(AGENCY_ID, response.getAgencyId());
        assertEquals(PLAN_ID, response.getPlanId());
    }

    @Test
    void getPaymentBySession_shouldThrowWhenPaymentNotFound() {
        givenAuthenticatedUser();
        when(paymentRepository.findByProviderCheckoutSessionId(CHECKOUT_SESSION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentBySession(USER_ID, CHECKOUT_SESSION_ID));
    }

    @Test
    void getPaymentBySession_shouldThrowWhenPaymentBelongsToAnotherAgency() {
        givenAuthenticatedUser();
        payment.setAgency(AgencyEntity.builder().id(UUID.randomUUID()).build());
        when(paymentRepository.findByProviderCheckoutSessionId(CHECKOUT_SESSION_ID))
                .thenReturn(Optional.of(payment));

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentBySession(USER_ID, CHECKOUT_SESSION_ID));
    }

    @Test
    void getPaymentBySession_shouldThrowWhenUserNotFound() {
        when(userRepository.findWithAgencyById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentBySession(USER_ID, CHECKOUT_SESSION_ID));

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void getPaymentBySession_shouldThrowWhenUserHasNoAgency() {
        user.setAgency(null);
        givenAuthenticatedUser();

        assertThrows(BadRequestException.class,
                () -> paymentService.getPaymentBySession(USER_ID, CHECKOUT_SESSION_ID));

        verifyNoInteractions(paymentRepository);
    }

    // ------------------------------------------------------------------
    // handleWebhook - success
    // ------------------------------------------------------------------

    @Test
    void handleWebhook_shouldActivateSubscriptionWhenPaymentSucceeds() {
        givenWebhook(succeededWebhook(5000L, CURRENCY));
        givenPaymentLocked();

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        ArgumentCaptor<AgencySubscriptionEntity> captor =
                ArgumentCaptor.forClass(AgencySubscriptionEntity.class);
        verify(subscriptionRepository).save(captor.capture());
        AgencySubscriptionEntity created = captor.getValue();

        assertSame(agency, created.getAgency());
        assertSame(plan, created.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, created.getStatus());
        assertEquals(LocalDate.now().plusDays(30), created.getEndDate());

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        assertEquals(CHECKOUT_SESSION_ID, payment.getProviderCheckoutSessionId());
        assertEquals(PAYMENT_INTENT_ID, payment.getProviderPaymentIntentId());
        assertNotNull(payment.getPaidAt());
        assertSame(created, payment.getSubscription());

        verify(paymentRepository).save(payment);
        verify(emailService).sendPaymentSuccessEmail(payment, created);
    }

    @Test
    void handleWebhook_shouldCancelPreviousSubscriptionWhenPaymentSucceeds() {
        givenWebhook(succeededWebhook(5000L, CURRENCY));
        givenPaymentLocked();

        AgencySubscriptionEntity old = AgencySubscriptionEntity.builder()
                .id(SUBSCRIPTION_ID)
                .agency(agency)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionRepository.findActiveForUpdate(
                eq(AGENCY_ID), eq(SubscriptionStatus.ACTIVE), any(PageRequest.class)))
                .thenReturn(List.of(old));

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(SubscriptionStatus.CANCELLED, old.getStatus());
        verify(subscriptionRepository).save(any(AgencySubscriptionEntity.class));
        verify(emailService).sendPaymentSuccessEmail(eq(payment), any(AgencySubscriptionEntity.class));
    }

    @Test
    void handleWebhook_shouldStaySucceededWhenSuccessEmailFails() {
        givenWebhook(succeededWebhook(5000L, CURRENCY));
        givenPaymentLocked();

        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailService)
                .sendPaymentSuccessEmail(any(PaymentEntity.class), any(AgencySubscriptionEntity.class));

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        verify(subscriptionRepository).save(any(AgencySubscriptionEntity.class));
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleWebhook_shouldRejectWhenAmountDoesNotMatch() {
        givenWebhook(succeededWebhook(4900L, CURRENCY));
        givenPaymentLocked();

        assertThrows(ConflictException.class,
                () -> paymentService.handleWebhook(PAYLOAD, SIGNATURE));

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verifyNoInteractions(subscriptionRepository, emailService);
    }

    @Test
    void handleWebhook_shouldRejectWhenCurrencyDoesNotMatch() {
        givenWebhook(succeededWebhook(5000L, "eur"));
        givenPaymentLocked();

        assertThrows(ConflictException.class,
                () -> paymentService.handleWebhook(PAYLOAD, SIGNATURE));

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verifyNoInteractions(subscriptionRepository, emailService);
    }

    @Test
    void handleWebhook_shouldRejectMissingAmount() {
        givenWebhook(succeededWebhook(null, CURRENCY));
        givenPaymentLocked();

        assertThrows(BadRequestException.class,
                () -> paymentService.handleWebhook(PAYLOAD, SIGNATURE));

        verifyNoInteractions(subscriptionRepository, emailService);
    }

    @Test
    void handleWebhook_shouldRejectMissingCurrency() {
        givenWebhook(succeededWebhook(5000L, null));
        givenPaymentLocked();

        assertThrows(BadRequestException.class,
                () -> paymentService.handleWebhook(PAYLOAD, SIGNATURE));

        verifyNoInteractions(subscriptionRepository, emailService);
    }

    @Test
    void handleWebhook_shouldRejectAmountWithMoreThanTwoDecimals() {
        payment.setAmount(new BigDecimal("50.001"));
        givenWebhook(succeededWebhook(5000L, CURRENCY));
        givenPaymentLocked();

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.handleWebhook(PAYLOAD, SIGNATURE));

        verifyNoInteractions(subscriptionRepository, emailService);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void handleWebhook_shouldIgnoreSuccessWhenPaymentIsNotPending(PaymentStatus status) {
        payment.setStatus(status);
        givenWebhook(succeededWebhook(5000L, CURRENCY));
        givenPaymentLocked();

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(status, payment.getStatus());
        verifyNoInteractions(subscriptionRepository, emailService);
    }

    // ------------------------------------------------------------------
    // handleWebhook - failed / canceled
    // ------------------------------------------------------------------

    @Test
    void handleWebhook_shouldMarkPaymentFailed() {
        givenWebhook(failedWebhook(PAYMENT_ID));
        givenPaymentLocked();

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("Card was declined.", payment.getFailureReason());
        assertEquals(PAYMENT_INTENT_ID, payment.getProviderPaymentIntentId());
        verify(paymentRepository).save(payment);
        verify(emailService).sendPaymentFailureEmail(payment);
    }

    @Test
    void handleWebhook_shouldStayFailedWhenFailureEmailFails() {
        givenWebhook(failedWebhook(PAYMENT_ID));
        givenPaymentLocked();

        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailService).sendPaymentFailureEmail(payment);

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void handleWebhook_shouldIgnoreFailureWhenPaymentIsNotPending(PaymentStatus status) {
        payment.setStatus(status);
        givenWebhook(failedWebhook(PAYMENT_ID));
        givenPaymentLocked();

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(status, payment.getStatus());
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void handleWebhook_shouldCancelPayment() {
        givenWebhook(canceledWebhook());
        givenPaymentLocked();

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
        assertEquals("Payment was not completed.", payment.getFailureReason());
        verify(paymentRepository).save(payment);
        verify(emailService).sendPaymentFailureEmail(payment);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void handleWebhook_shouldIgnoreCancelWhenPaymentIsNotPending(PaymentStatus status) {
        payment.setStatus(status);
        givenWebhook(canceledWebhook());
        givenPaymentLocked();

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(status, payment.getStatus());
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
        verifyNoInteractions(emailService);
    }

    // ------------------------------------------------------------------
    // handleWebhook - routing, duplicates, lookups
    // ------------------------------------------------------------------

    @Test
    void handleWebhook_shouldIgnoreDuplicateEvent() {
        PaymentWebhookData data = succeededWebhook(5000L, CURRENCY);
        when(paymentGateway.parseWebhook(PAYLOAD, SIGNATURE)).thenReturn(data);
        when(webhookEventRepository.insertIfAbsent(
                any(UUID.class), eq(data.eventId()), eq(data.eventType()), any(LocalDateTime.class)))
                .thenReturn(0);

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        verifyNoInteractions(paymentRepository, subscriptionRepository, emailService);
    }

    @Test
    void handleWebhook_shouldIgnoreUnsupportedEvent() {
        givenWebhook(new PaymentWebhookData(
                "evt_1", "customer.created", PaymentWebhookType.UNSUPPORTED,
                null, null, null, null, null, null));

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        verifyNoInteractions(paymentRepository, subscriptionRepository, emailService);
    }

    @Test
    void handleWebhook_shouldThrowWhenPaymentNotFound() {
        givenWebhook(succeededWebhook(5000L, CURRENCY));
        when(paymentRepository.findByIdForUpdate(PAYMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.handleWebhook(PAYLOAD, SIGNATURE));

        verifyNoInteractions(subscriptionRepository, emailService);
    }

    @Test
    void handleWebhook_shouldFindPaymentByPaymentIntentWhenPaymentIdIsMissing() {
        givenWebhook(failedWebhook(null));
        when(paymentRepository.findByProviderPaymentIntentId(PAYMENT_INTENT_ID))
                .thenReturn(Optional.of(payment));

        paymentService.handleWebhook(PAYLOAD, SIGNATURE);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void handleWebhook_shouldThrowWhenPaymentIntentNotFound() {
        givenWebhook(failedWebhook(null));
        when(paymentRepository.findByProviderPaymentIntentId(PAYMENT_INTENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.handleWebhook(PAYLOAD, SIGNATURE));

        verifyNoInteractions(subscriptionRepository, emailService);
    }

    @Test
    void handleWebhook_shouldThrowWhenNoPaymentIdentifierIsPresent() {
        givenWebhook(new PaymentWebhookData(
                "evt_1", "checkout.session.completed", PaymentWebhookType.PAYMENT_SUCCEEDED,
                null, null, null, 5000L, CURRENCY, null));

        assertThrows(BadRequestException.class,
                () -> paymentService.handleWebhook(PAYLOAD, SIGNATURE));

        verifyNoInteractions(subscriptionRepository, emailService);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private void givenAuthenticatedUser() {
        when(userRepository.findWithAgencyById(USER_ID)).thenReturn(Optional.of(user));
    }

    private void givenPaymentLocked() {
        when(paymentRepository.findByIdForUpdate(PAYMENT_ID)).thenReturn(Optional.of(payment));
    }

    /** Gateway returns the webhook and the event is recorded as new. */
    private void givenWebhook(PaymentWebhookData data) {
        when(paymentGateway.parseWebhook(PAYLOAD, SIGNATURE)).thenReturn(data);
        when(webhookEventRepository.insertIfAbsent(
                any(UUID.class), eq(data.eventId()), eq(data.eventType()), any(LocalDateTime.class)))
                .thenReturn(1);
    }

    private PaymentWebhookData succeededWebhook(Long amountMinor, String currency) {
        return new PaymentWebhookData(
                "evt_success", "checkout.session.completed", PaymentWebhookType.PAYMENT_SUCCEEDED,
                PAYMENT_ID, CHECKOUT_SESSION_ID, PAYMENT_INTENT_ID, amountMinor, currency, null);
    }

    private PaymentWebhookData failedWebhook(UUID paymentId) {
        return new PaymentWebhookData(
                "evt_failed", "payment_intent.payment_failed", PaymentWebhookType.PAYMENT_FAILED,
                paymentId, null, PAYMENT_INTENT_ID, 5000L, CURRENCY, "Card was declined.");
    }

    private PaymentWebhookData canceledWebhook() {
        return new PaymentWebhookData(
                "evt_canceled", "checkout.session.expired", PaymentWebhookType.PAYMENT_CANCELED,
                PAYMENT_ID, CHECKOUT_SESSION_ID, null, 5000L, CURRENCY, "Payment was not completed.");
    }
}