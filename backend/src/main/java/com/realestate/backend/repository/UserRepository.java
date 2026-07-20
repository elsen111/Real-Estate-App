package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findWithRolesById(UUID id);

    Boolean existsByEmail(String email);

    long countByAgency(AgencyEntity agency);

    @Query("SELECT am FROM AgencyMemberEntity am " +
            "JOIN FETCH am.user u " +
            "JOIN FETCH am.agency a " +
            "JOIN u.roles r " +
            "WHERE u.id = :userId " +
            "AND r.roleName = com.realestate.backend.enums.Role.AGENT")
    Optional<AgencyMemberEntity> findAgentMemberByUserId(@Param("userId") UUID userId);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

}
