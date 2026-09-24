package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.enums.SubscriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencySubscriptionRepository
        extends JpaRepository<AgencySubscriptionEntity, UUID> {

    Optional<AgencySubscriptionEntity>
    findFirstByAgency_IdAndStatusOrderByEndDateDesc(
            UUID agencyId,
            SubscriptionStatus status
    );

    Optional<AgencySubscriptionEntity>
    findFirstByAgencyIdOrderByEndDateDesc(
            UUID agencyId,
            SubscriptionStatus status
    );

    boolean existsByAgencyIdAndStatus(
            UUID agencyId,
            SubscriptionStatus status
    );

    boolean existsByPlanIdAndStatus(
            UUID planId,
            SubscriptionStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM AgencySubscriptionEntity s
            WHERE s.agency.id = :agencyId
            AND s.status = :status
            ORDER BY s.endDate DESC
            """)
    List<AgencySubscriptionEntity> findActiveForUpdate(
            @Param("agencyId") UUID agencyId,
            @Param("status") SubscriptionStatus status,
            PageRequest pageable
    );

    Optional<AgencySubscriptionEntity> findFirstByAgencyIdAndStatusOrderByEndDateDesc(
            UUID agencyId,
            SubscriptionStatus status
    );

    Optional<AgencySubscriptionEntity>
    findByAgencyAndStatus(
            AgencyEntity agency,
            SubscriptionStatus status
    );

    List<AgencySubscriptionEntity>
    findByStatusAndEndDate(
            SubscriptionStatus subscriptionStatus,
            LocalDate targetDate,
            PageRequest pageable
    );

    List<AgencySubscriptionEntity>
    findByStatusAndEndDateBefore(
            SubscriptionStatus subscriptionStatus,
            LocalDate today,
            PageRequest pageable
    );
}