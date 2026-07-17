package com.realestate.backend.repository;

import com.realestate.backend.entity.PropertyMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyMediaRepository extends JpaRepository<PropertyMediaEntity, UUID> {

    List<PropertyMediaEntity> findByPropertyIdOrderBySortOrder(UUID propertyId);

}
