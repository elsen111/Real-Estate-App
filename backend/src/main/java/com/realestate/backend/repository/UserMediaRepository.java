package com.realestate.backend.repository;

import com.realestate.backend.entity.UserMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserMediaRepository extends JpaRepository<UserMediaEntity, UUID> {

    Optional<UserMediaEntity> findByUserId(UUID uuid);

}
