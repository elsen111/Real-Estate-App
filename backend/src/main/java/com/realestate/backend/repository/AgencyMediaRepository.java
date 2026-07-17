package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgencyMediaRepository extends JpaRepository<AgencyMediaEntity, UUID> {

    Optional<AgencyMediaEntity> findByAgencyId(UUID agencyId);

}
