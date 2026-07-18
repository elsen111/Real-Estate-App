package com.realestate.backend.repository;

import com.realestate.backend.entity.PropertyMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PropertyMediaRepository extends JpaRepository<PropertyMediaEntity, UUID> {

    List<PropertyMediaEntity> findByPropertyIdOrderBySortOrder(UUID propertyId);

    boolean existsByPropertyIdAndIsPrimaryTrue(UUID propertyId);

    long countByPropertyId(UUID propertyId);

    @Query("""
       SELECT MAX(pm.sortOrder)
       FROM PropertyMediaEntity pm
       WHERE pm.property.id = :propertyId
       """)
    Integer findMaxSortOrder(UUID propertyId);

    List<PropertyMediaEntity> findByPropertyIdOrderBySortOrderAsc(
            UUID propertyId
    );

    List<PropertyMediaEntity> findByPropertyIdInAndIsPrimaryTrue(List<UUID> propertyIds);

    PropertyMediaEntity findByPropertyIdAndIsPrimary(UUID propertyId, boolean isPrimary);

}
