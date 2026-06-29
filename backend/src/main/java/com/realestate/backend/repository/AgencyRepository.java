package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.enums.AgencyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyRepository extends JpaRepository<AgencyEntity, UUID>, JpaSpecificationExecutor<AgencyEntity> {

    boolean existsByEmail(String email);

    Page<AgencyEntity> findByEmail(String email, Pageable pageable);

    Page<AgencyEntity> findByName(String name, Pageable pageable);

    Page<AgencyEntity> findByStatus(AgencyStatus status, Pageable pageable);

    Page<AgencyEntity> findByCity(String city, Pageable pageable);

    Optional<AgencyEntity> findByEmail(String email);

}
