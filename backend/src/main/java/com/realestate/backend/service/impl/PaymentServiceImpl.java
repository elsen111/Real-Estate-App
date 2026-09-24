package com.realestate.backend.service.impl;

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
import com.realestate.backend.service.EmailService;
import com.realestate.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AgencySubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final PaymentGateway paymentGateway;
    private final EmailService emailService;

    @Value("${payment.stripe.currency}")
    private String currency;

    @Override
    public PaymentCheckoutResponse createCheckout(
            UUID userId,
            UUID planId,
            String idempotencyKey
    ) {

        validateIdempotencyKey(idempotencyKey);

        UserEntity user =
                userRepository.findWithAgencyById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated user not found."
                                )
                        );

        AgencyEntity agency = user.getAgency();

        if (agency == null) {
            throw new BadRequestException(
                    "Authenticated user is not associated with an agency."
            );
        }

        if (Boolean.TRUE.equals(agency.getIsDeleted())) {
            throw new BadRequestException(
                    "Agency has been deleted."
            );
        }

        if (!StringUtils.hasText(agency.getEmail())) {
            throw new BadRequestException(
                    "Agency email is required for payment notifications."
            );
        }

        PaymentEntity existingPayment =
                paymentRepository
                        .findByAgencyIdAndIdempotencyKey(
                                agency.getId(),
                                idempotencyKey
                        )
                        .orElse(null);

        if (existingPayment != null) {
            validateExistingPayment(
                    existingPayment,
                    planId
            );

            return toCheckoutResponse(existingPayment);
        }

        SubscriptionPlanEntity plan =
                subscriptionPlanRepository
                        .findByIdAndActiveTrueAndDeletedFalse(planId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Subscription plan not found."
                                )
                        );

        validateActiveSubscription(
                agency.getId(),
                plan.getId()
        );

        if (paymentRepository.existsByAgencyIdAndStatus(
                agency.getId(),
                PaymentStatus.PENDING
        )) {
            throw new ConflictException(
                    "Agency already has a pending payment."
            );
        }

        PaymentEntity payment =
                PaymentEntity.builder()
                        .agency(agency)
                        .plan(plan)
                        .idempotencyKey(idempotencyKey)
                        .amount(plan.getPrice())
                        .currency(currency.toLowerCase())
                        .status(PaymentStatus.PENDING)
                        .build();

        try {

            paymentRepository.saveAndFlush(payment);

        } catch (DataIntegrityViolationException e) {

            PaymentEntity concurrentPayment =
                    paymentRepository
                            .findByAgencyIdAndIdempotencyKey(
                                    agency.getId(),
                                    idempotencyKey
                            )
                            .orElseThrow(() -> e);

            validateExistingPayment(
                    concurrentPayment,
                    planId
            );

            return toCheckoutResponse(concurrentPayment);
        }

        try {

            PaymentCheckoutResult result =
                    paymentGateway.createCheckout(
                            payment,
                            agency,
                            plan
                    );

            payment.setProviderCheckoutSessionId(
                    result.checkoutSessionId()
            );

            payment.setProviderPaymentIntentId(
                    result.paymentIntentId()
            );

            payment.setCheckoutUrl(
                    result.checkoutUrl()
            );

            payment.setPlan(plan);

            paymentRepository.save(payment);

            log.atInfo()
                    .setMessage("Payment checkout created")
                    .addKeyValue(
                            "paymentId",
                            payment.getId()
                    )
                    .addKeyValue(
                            "agencyId",
                            agency.getId()
                    )
                    .addKeyValue(
                            "planId",
                            plan.getId()
                    )
                    .log();

            return PaymentCheckoutResponse.builder()
                    .paymentId(payment.getId())
                    .checkoutUrl(result.checkoutUrl())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .status(payment.getStatus())
                    .build();

        } catch (PaymentProcessingException e) {

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());

            paymentRepository.save(payment);

            sendPaymentFailureEmailSafely(payment);

            throw e;
        }
    }

    @Override
    @Transactional
    public void handleWebhook(
            String payload,
            String signature
    ) {

        PaymentWebhookData webhook =
                paymentGateway.parseWebhook(
                        payload,
                        signature
                );

        int inserted =
                webhookEventRepository.insertIfAbsent(
                        UUID.randomUUID(),
                        webhook.eventId(),
                        webhook.eventType(),
                        LocalDateTime.now()
                );

        if (inserted == 0) {
            log.atInfo()
                    .setMessage("Duplicate payment webhook ignored")
                    .addKeyValue(
                            "eventId",
                            webhook.eventId()
                    )
                    .log();

            return;
        }

        if (webhook.type() == PaymentWebhookType.UNSUPPORTED) {
            return;
        }

        PaymentEntity payment =
                findPaymentForWebhook(webhook);

        switch (webhook.type()) {

            case PAYMENT_SUCCEEDED ->
                    processSuccessfulPayment(
                            payment,
                            webhook
                    );

            case PAYMENT_FAILED ->
                    processFailedPayment(
                            payment,
                            webhook
                    );

            case PAYMENT_CANCELED ->
                    processCanceledPayment(
                            payment,
                            webhook
                    );

            case UNSUPPORTED -> {
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
            UUID userId,
            UUID paymentId
    ) {

        UserEntity user =
                userRepository.findWithAgencyById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated user not found."
                                )
                        );

        if (user.getAgency() == null) {
            throw new BadRequestException(
                    "Authenticated user is not associated with an agency."
            );
        }

        PaymentEntity payment =
                paymentRepository
                        .findByIdAndAgencyId(
                                paymentId,
                                user.getAgency().getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found."
                                )
                        );

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentBySession(
            UUID userId,
            String checkoutSessionId
    ) {

        UserEntity user =
                userRepository.findWithAgencyById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated user not found."
                                )
                        );

        if (user.getAgency() == null) {
            throw new BadRequestException(
                    "Authenticated user is not associated with an agency."
            );
        }

        PaymentEntity payment =
                paymentRepository
                        .findByProviderCheckoutSessionId(
                                checkoutSessionId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found."
                                )
                        );

        if (!payment.getAgency()
                .getId()
                .equals(user.getAgency().getId())) {

            throw new ResourceNotFoundException(
                    "Payment not found."
            );
        }

        return toResponse(payment);
    }

    private PaymentEntity findPaymentForWebhook(
            PaymentWebhookData webhook
    ) {

        if (webhook.paymentId() != null) {

            return paymentRepository
                    .findByIdForUpdate(
                            webhook.paymentId()
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Payment referenced by webhook was not found."
                            )
                    );
        }

        if (webhook.paymentIntentId() != null) {

            return paymentRepository
                    .findByProviderPaymentIntentId(
                            webhook.paymentIntentId()
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Payment referenced by webhook was not found."
                            )
                    );
        }

        throw new BadRequestException(
                "Payment identifier is missing from webhook."
        );
    }

    private void processSuccessfulPayment(
            PaymentEntity payment,
            PaymentWebhookData webhook
    ) {

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        validateWebhookAmountAndCurrency(
                payment,
                webhook
        );

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setFailureReason(null);
        payment.setProviderCheckoutSessionId(
                webhook.checkoutSessionId()
        );
        payment.setProviderPaymentIntentId(
                webhook.paymentIntentId()
        );
        payment.setPaidAt(LocalDateTime.now());

        AgencySubscriptionEntity oldSubscription =
                findActiveSubscription(payment.getAgency().getId());

        if (oldSubscription != null) {

            oldSubscription.setStatus(
                    SubscriptionStatus.CANCELLED
            );
        }

        LocalDate startDate = LocalDate.now();

        LocalDate endDate =
                startDate.plusDays(
                        payment.getPlan().getDurationDays()
                );

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .agency(payment.getAgency())
                        .plan(payment.getPlan())
                        .startDate(startDate)
                        .endDate(endDate)
                        .status(SubscriptionStatus.ACTIVE)
                        .build();

        subscriptionRepository.save(subscription);

        payment.setSubscription(subscription);

        paymentRepository.save(payment);

        sendPaymentSuccessEmailSafely(
                payment,
                subscription
        );

        log.atInfo()
                .setMessage("Payment succeeded and subscription activated")
                .addKeyValue(
                        "paymentId",
                        payment.getId()
                )
                .addKeyValue(
                        "subscriptionId",
                        subscription.getId()
                )
                .addKeyValue(
                        "agencyId",
                        payment.getAgency().getId()
                )
                .addKeyValue(
                        "planId",
                        payment.getPlan().getId()
                )
                .log();
    }

    private void processFailedPayment(
            PaymentEntity payment,
            PaymentWebhookData webhook
    ) {

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(
                webhook.failureReason()
        );

        if (webhook.paymentIntentId() != null) {
            payment.setProviderPaymentIntentId(
                    webhook.paymentIntentId()
            );
        }

        paymentRepository.save(payment);

        sendPaymentFailureEmailSafely(payment);

        log.atWarn()
                .setMessage("Payment failed")
                .addKeyValue(
                        "paymentId",
                        payment.getId()
                )
                .addKeyValue(
                        "agencyId",
                        payment.getAgency().getId()
                )
                .log();
    }

    private void processCanceledPayment(
            PaymentEntity payment,
            PaymentWebhookData webhook
    ) {

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.setStatus(PaymentStatus.CANCELED);
        payment.setFailureReason(
                webhook.failureReason()
        );

        paymentRepository.save(payment);

        sendPaymentFailureEmailSafely(payment);

        log.atInfo()
                .setMessage("Payment checkout canceled")
                .addKeyValue(
                        "paymentId",
                        payment.getId()
                )
                .log();
    }

    private AgencySubscriptionEntity findActiveSubscription(
            UUID agencyId
    ) {

        return subscriptionRepository
                .findActiveForUpdate(
                        agencyId,
                        SubscriptionStatus.ACTIVE,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private void validateActiveSubscription(
            UUID agencyId,
            UUID newPlanId
    ) {

        AgencySubscriptionEntity activeSubscription =
                subscriptionRepository
                        .findFirstByAgencyIdAndStatusOrderByEndDateDesc(
                                agencyId,
                                SubscriptionStatus.ACTIVE
                        )
                        .orElse(null);

        if (activeSubscription == null) {
            return;
        }

        if (activeSubscription
                .getPlan()
                .getId()
                .equals(newPlanId)) {

            throw new ConflictException(
                    "Agency already has this subscription plan."
            );
        }
    }

    private void validateExistingPayment(
            PaymentEntity payment,
            UUID requestedPlanId
    ) {

        if (!payment.getPlan()
                .getId()
                .equals(requestedPlanId)) {

            throw new ConflictException(
                    "Idempotency key was already used for another subscription plan."
            );
        }
    }

    private void validateWebhookAmountAndCurrency(
            PaymentEntity payment,
            PaymentWebhookData webhook
    ) {

        if (webhook.amountMinor() == null ||
                webhook.currency() == null) {

            throw new BadRequestException(
                    "Payment amount or currency is missing."
            );
        }

        long expectedAmount;

        try {

            expectedAmount =
                    payment.getAmount()
                            .movePointRight(2)
                            .longValueExact();

        } catch (ArithmeticException e) {

            throw new PaymentProcessingException(
                    "Invalid payment amount.",
                    e
            );
        }

        if (expectedAmount != webhook.amountMinor()) {

            throw new ConflictException(
                    "Payment amount does not match the subscription price."
            );
        }

        if (!payment.getCurrency()
                .equalsIgnoreCase(webhook.currency())) {

            throw new ConflictException(
                    "Payment currency does not match the subscription currency."
            );
        }
    }

    private void sendPaymentSuccessEmailSafely(
            PaymentEntity payment,
            AgencySubscriptionEntity subscription
    ) {

        try {

            emailService.sendPaymentSuccessEmail(
                    payment,
                    subscription
            );

        } catch (Exception e) {

            log.atError()
                    .setMessage(
                            "Payment succeeded but success email could not be sent"
                    )
                    .addKeyValue(
                            "paymentId",
                            payment.getId()
                    )
                    .setCause(e)
                    .log();
        }
    }

    private void sendPaymentFailureEmailSafely(
            PaymentEntity payment
    ) {

        try {

            emailService.sendPaymentFailureEmail(
                    payment
            );

        } catch (Exception e) {

            log.atError()
                    .setMessage(
                            "Payment status updated but failure email could not be sent"
                    )
                    .addKeyValue(
                            "paymentId",
                            payment.getId()
                    )
                    .setCause(e)
                    .log();
        }
    }

    private PaymentCheckoutResponse toCheckoutResponse(
            PaymentEntity payment
    ) {

        return PaymentCheckoutResponse.builder()
                .paymentId(payment.getId())
                .checkoutUrl(payment.getCheckoutUrl())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .build();
    }

    private String buildCheckoutUrl(
            PaymentEntity payment
    ) {
        return payment.getCheckoutUrl();
    }

    private PaymentResponse toResponse(
            PaymentEntity payment
    ) {

        return PaymentResponse.builder()
                .id(payment.getId())
                .agencyId(payment.getAgency().getId())
                .planId(payment.getPlan().getId())
                .planName(payment.getPlan().getName())
                .subscriptionId(
                        payment.getSubscription() == null
                                ? null
                                : payment.getSubscription().getId()
                )
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private void validateIdempotencyKey(
            String idempotencyKey
    ) {

        if (!StringUtils.hasText(idempotencyKey)) {
            throw new BadRequestException(
                    "Idempotency-Key header is required."
            );
        }

        if (idempotencyKey.length() > 255) {
            throw new BadRequestException(
                    "Idempotency-Key must not exceed 255 characters."
            );
        }
    }
}