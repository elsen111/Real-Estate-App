package com.realestate.backend.repository;

import com.realestate.backend.entity.AppointmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    Page<AppointmentEntity> findByClient_Id(UUID clientId, Pageable pageable);

    Page<AppointmentEntity> findByProperty_Id(UUID propertyId, Pageable pageable);

    Page<AppointmentEntity> findByAgent_Id(UUID agentId, Pageable pageable);

}
