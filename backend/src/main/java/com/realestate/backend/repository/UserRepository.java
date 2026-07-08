package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findWithRolesById(UUID id);

    Boolean existsByEmail(String email);

    long countByAgency(AgencyEntity agency);

}
