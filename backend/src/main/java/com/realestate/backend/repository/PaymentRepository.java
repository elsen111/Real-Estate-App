package com.realestate.backend.repository;

import com.realestate.backend.entity.PaymentEntity;
import com.realestate.backend.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository
        extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByAgencyIdAndIdempotencyKey(
            UUID agencyId,
            String idempotencyKey
    );

    Optional<PaymentEntity> findByProviderCheckoutSessionId(
            String providerCheckoutSessionId
    );

    Optional<PaymentEntity> findByProviderPaymentIntentId(
            String providerPaymentIntentId
    );

    Optional<PaymentEntity> findByIdAndAgencyId(
            UUID paymentId,
            UUID agencyId
    );

    boolean existsByAgencyIdAndStatus(
            UUID agencyId,
            PaymentStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM PaymentEntity p
            WHERE p.id = :paymentId
            """)
    Optional<PaymentEntity> findByIdForUpdate(
            @Param("paymentId") UUID paymentId
    );
}