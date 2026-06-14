package com.realestate.backend.repository;

import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByRoleName(Role roleName);

    boolean existsByRoleName(Role roleName);
}
