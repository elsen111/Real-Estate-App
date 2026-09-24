package com.realestate.backend.entity;

import com.realestate.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_agency_id", columnList = "agency_id"),
                @Index(name = "idx_payments_plan_id", columnList = "plan_id"),
                @Index(name = "idx_payments_status", columnList = "status"),
                @Index(name = "idx_payments_agency_status", columnList = "agency_id, status"),
                @Index(name = "idx_payments_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "agency_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_agency_id")
    )
    private AgencyEntity agency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "plan_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_plan_id")
    )
    private SubscriptionPlanEntity plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "subscription_id",
            foreignKey = @ForeignKey(name = "fk_payments_subscription_id")
    )
    private AgencySubscriptionEntity subscription;

    @Column(
            name = "idempotency_key",
            nullable = false,
            length = 255
    )
    private String idempotencyKey;

    @Column(
            name = "provider_checkout_session_id",
            unique = true,
            length = 255
    )
    private String providerCheckoutSessionId;

    @Column(
            name = "provider_payment_intent_id",
            unique = true,
            length = 255
    )
    private String providerPaymentIntentId;

    @Column(
            name = "checkout_url",
            length = 1000
    )
    private String checkoutUrl;

    @Column(
            name = "amount",
            precision = 10,
            scale = 2,
            nullable = false
    )
    private BigDecimal amount;

    @Column(
            name = "currency",
            length = 3,
            nullable = false
    )
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 30,
            nullable = false
    )
    private PaymentStatus status;

    @Column(
            name = "failure_reason",
            columnDefinition = "TEXT"
    )
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
}