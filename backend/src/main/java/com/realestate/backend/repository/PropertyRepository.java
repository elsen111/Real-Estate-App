package com.realestate.backend.repository;

import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID>,
        JpaSpecificationExecutor<PropertyEntity> {

    Page<PropertyEntity> findByAgency_Id(UUID agencyId, Pageable pageable);

    Page<PropertyEntity> findByCategory_Id(UUID categoryId, Pageable pageable);

    Page<PropertyEntity> findByStatus(PropertyStatus status, Pageable pageable);

    Page<PropertyEntity> findByCityIgnoreCase(String city, Pageable pageable);

    boolean existsByIdAndAgency_Id(UUID id, UUID agencyId);

    long countByAgencyId(UUID agencyId);

    long countByAgencyIdAndStatus(
            UUID agencyId,
            PropertyStatus status
        );

    List<PropertyEntity> findByAgencyId(UUID agencyId);


}
