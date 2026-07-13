package com.realestate.backend.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    @Modifying
    @Query("UPDATE PropertyEntity p SET p.assignedAgent = null WHERE p.assignedAgent.id = :agentId")
    int unassignAgentFromAllProperties(@Param("agentId") UUID agentId);

    @Query("""
        SELECT p FROM PropertyEntity p
        LEFT JOIN FETCH p.agency
        LEFT JOIN FETCH p.assignedAgent
        LEFT JOIN FETCH p.category
        WHERE p.id = :id
    """)
    Optional<PropertyEntity> findByIdWithDetails(@Param("id") UUID id);

}
