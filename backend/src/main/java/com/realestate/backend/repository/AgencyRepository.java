package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AgencyRepository extends JpaRepository<AgencyEntity, UUID>, JpaSpecificationExecutor<AgencyEntity> {

    boolean existsByEmail(
            String email
    );

}
