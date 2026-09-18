package com.realestate.backend.entity;

import com.realestate.backend.enums.NotificationStatus;
import com.realestate.backend.enums.SubscriptionNotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "subscription_notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subscription_notification_subscription_type",
                        columnNames = {"subscription_id", "type"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_subscription_notification_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_subscription_notification_status_next_attempt",
                        columnList = "status, next_attempt_at"
                ),
                @Index(
                        name = "idx_subscription_notification_locked_at",
                        columnList = "locked_at"
                )
        }
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "subscription_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscription_notification_subscription")
    )
    private AgencySubscriptionEntity subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionNotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = NotificationStatus.PENDING;
        }

        if (nextAttemptAt == null) {
            nextAttemptAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}