package com.realestate.backend.repository;

import com.realestate.backend.entity.PaymentWebhookEventEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PaymentWebhookEventRepository
        extends JpaRepository<PaymentWebhookEventEntity, UUID> {

    @Modifying
    @Query(
            value = """
                    INSERT INTO payment_webhook_events (
                        id,
                        provider_event_id,
                        event_type,
                        processed_at
                    )
                    VALUES (
                        :id,
                        :providerEventId,
                        :eventType,
                        :processedAt
                    )
                    ON CONFLICT (provider_event_id) DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("providerEventId") String providerEventId,
            @Param("eventType") String eventType,
            @Param("processedAt") LocalDateTime processedAt
    );
}