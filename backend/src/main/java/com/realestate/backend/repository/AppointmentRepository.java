package com.realestate.backend.repository;

import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    Page<AppointmentEntity> findByClient_Id(UUID clientId, Pageable pageable);

    Page<AppointmentEntity> findByProperty_Id(UUID propertyId, Pageable pageable);

    Page<AppointmentEntity> findByAgent_Id(UUID agentId, Pageable pageable);

    boolean existsByPropertyIdAndClientIdAndStatus(UUID propertyId, UUID clientId, AppointmentStatus status);

    boolean existsByClientIdAndId(UUID clientId, UUID appointmentId);

    @EntityGraph(attributePaths = {"property", "client", "agent", "agency"})
    Page<AppointmentEntity> findByClientId(UUID clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"property", "client", "agent", "agency"})
    Page<AppointmentEntity> findByClientIdAndStatus(UUID clientId, AppointmentStatus status, Pageable pageable);

}
