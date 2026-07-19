package com.realestate.backend.repository;

import com.realestate.backend.entity.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<MediaFileEntity, UUID> {
}