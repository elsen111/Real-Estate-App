package com.realestate.backend.repository;

import com.realestate.backend.entity.InquiryEntity;
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
public interface InquiryRepository extends JpaRepository<InquiryEntity, UUID> {

    @EntityGraph(attributePaths = {"property", "client", "assignedAgent", "agency"})
    Page<InquiryEntity> findByClientId(UUID clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"property", "client", "assignedAgent", "agency"})
    Page<InquiryEntity> findByClientIdAndStatus(UUID clientId, InquiryStatus status, Pageable pageable);

    boolean existsByPropertyIdAndClientIdAndStatusNot(UUID propertyId, UUID clientId, InquiryStatus status);

    @Query(value = """
    SELECT i FROM InquiryEntity i
    JOIN FETCH i.property p
    JOIN FETCH i.client c
    LEFT JOIN FETCH i.assignedAgent
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
    Page<InquiryEntity> findByAgencyIdWithFilters(
            @Param("agencyId") UUID agencyId,
            @Param("status") InquiryStatus status,
            @Param("propertyId") UUID propertyId,
            Pageable pageable);

}
