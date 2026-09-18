package com.realestate.backend.repository;

import com.realestate.backend.entity.SubscriptionNotificationEntity;
import com.realestate.backend.enums.NotificationStatus;
import com.realestate.backend.enums.SubscriptionNotificationType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionNotificationRepository
        extends JpaRepository<SubscriptionNotificationEntity, UUID> {

    boolean existsBySubscriptionIdAndType(
            UUID subscriptionId,
            SubscriptionNotificationType type
    );

    List<SubscriptionNotificationEntity>
    findByStatusOrderByCreatedAtAsc(
            NotificationStatus status,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE SubscriptionNotificationEntity n
               SET n.status = :processing,
                   n.lockedAt = :lockedAt,
                   n.attempts = n.attempts + 1
             WHERE n.id = :id
               AND n.status = :pending
            """)
    int claimNotification(
            @Param("id") UUID id,
            @Param("pending") NotificationStatus pending,
            @Param("processing") NotificationStatus processing,
            @Param("lockedAt") LocalDateTime lockedAt
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE SubscriptionNotificationEntity n
               SET n.status = :pending,
                   n.lockedAt = null
             WHERE n.status = :processing
               AND n.lockedAt < :threshold
            """)
    int releaseStuckNotifications(
            @Param("processing") NotificationStatus processing,
            @Param("pending") NotificationStatus pending,
            @Param("threshold") LocalDateTime threshold
    );
}