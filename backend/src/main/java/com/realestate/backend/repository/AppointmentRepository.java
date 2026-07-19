package com.realestate.backend.repository;

import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    boolean existsByPropertyIdAndClientIdAndStatus(UUID propertyId, UUID clientId, AppointmentStatus status);

    @EntityGraph(attributePaths = {"property", "client", "agent", "agency"})
    Page<AppointmentEntity> findByClientId(UUID clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"property", "client", "agent", "agency"})
    Page<AppointmentEntity> findByClientIdAndStatus(UUID clientId, AppointmentStatus status, Pageable pageable);

    @Query(value = """
    SELECT i FROM AppointmentEntity i
    JOIN FETCH i.property p
    JOIN FETCH i.client c
    LEFT JOIN FETCH i.agent
    JOIN FETCH i.agency
    WHERE i.agency.id = :agencyId
    AND (:status IS NULL OR i.status = :status)
    AND (:propertyId IS NULL OR i.property.id = :propertyId)
    ORDER BY i.createdAt DESC
    """,
            countQuery = """
    SELECT COUNT(i) FROM InquiryEntity i
    WHERE i.agency.id = :agencyId
    AND (:status IS NULL OR i.status = :status)
    AND (:propertyId IS NULL OR i.property.id = :propertyId)
    """)
    Page<AppointmentEntity> findByAgencyIdWithFilters(
            @Param("agencyId") UUID agencyId,
            @Param("status") AppointmentStatus status,
            @Param("propertyId") UUID propertyId,
            Pageable pageable);

}
