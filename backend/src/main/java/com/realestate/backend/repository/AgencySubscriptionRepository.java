package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencySubscriptionRepository extends JpaRepository<AgencySubscriptionEntity, UUID> {

    Optional<AgencySubscriptionEntity> findFirstByAgency_IdAndStatusOrderByEndDateDesc(UUID agencyId, SubscriptionStatus status);

    Optional<AgencySubscriptionEntity> findFirstByAgencyIdOrderByEndDateDesc(UUID agencyId, SubscriptionStatus status);

    boolean existsByAgencyIdAndStatus(UUID agencyId, SubscriptionStatus status);

    boolean existsByPlanIdAndStatus(UUID planId, SubscriptionStatus status);

    Optional<AgencySubscriptionEntity> findFirstByAgencyIdAndStatusOrderByEndDateDesc(
            UUID agencyId,
            SubscriptionStatus status
    );

    Optional<AgencySubscriptionEntity> findByAgencyAndStatus(
            AgencyEntity agency,
            SubscriptionStatus status
    );

    List<AgencySubscriptionEntity> findByStatusAndEndDate(SubscriptionStatus subscriptionStatus, LocalDate targetDate, PageRequest of);

    List<AgencySubscriptionEntity> findByStatusAndEndDateBefore(SubscriptionStatus subscriptionStatus, LocalDate today, PageRequest of);
}
