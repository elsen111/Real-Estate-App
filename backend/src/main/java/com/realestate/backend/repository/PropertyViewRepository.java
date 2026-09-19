package com.realestate.backend.repository;

import com.realestate.backend.entity.PropertyViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PropertyViewRepository extends JpaRepository<PropertyViewEntity, UUID> {

    boolean existsByPropertyIdAndViewerIdAndViewedAtAfter(
            UUID propertyId,
            UUID viewerId,
            LocalDateTime threshold
    );

}
