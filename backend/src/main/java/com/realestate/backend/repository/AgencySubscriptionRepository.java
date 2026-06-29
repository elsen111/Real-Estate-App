package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencySubscriptionRepository extends JpaRepository<AgencySubscriptionEntity, UUID> {

    Optional<AgencySubscriptionEntity> findFirstByAgency_IdAndStatusOrderByEndDateDesc(UUID agencyId, SubscriptionStatus status);

    Optional<AgencySubscriptionEntity> findFirstByAgencyIdOrderByEndDateDesc(UUID agencyId, SubscriptionStatus status);

    boolean existsByAgency_IdAndStatusAndEndDateAfter(UUID agencyId, SubscriptionStatus status, LocalDate endDate);

    Page<AgencySubscriptionEntity> findByAgency_IdOrderByCreatedAtDesc(UUID agencyId, Pageable pageable);

}
