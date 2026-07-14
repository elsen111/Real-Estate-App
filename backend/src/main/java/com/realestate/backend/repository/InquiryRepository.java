package com.realestate.backend.repository;

import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.enums.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InquiryRepository extends JpaRepository<InquiryEntity, UUID> {

    @EntityGraph(attributePaths = {"property", "client", "assignedAgent", "agency"})
    Page<InquiryEntity> findByClientId(UUID clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"property", "client", "assignedAgent", "agency"})
    Page<InquiryEntity> findByClientIdAndStatus(UUID clientId, InquiryStatus status, Pageable pageable);

    Page<InquiryEntity> findByProperty_Id(UUID propertyId, Pageable pageable);

    Page<InquiryEntity> findByAssignedAgent_Id(UUID assignedAgentId, Pageable pageable);

    boolean existsByPropertyIdAndClientIdAndStatusNot(UUID propertyId, UUID clientId, InquiryStatus status);
}
