package com.realestate.backend.repository;

import com.realestate.backend.dto.response.PropertySuggestionResponse;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.PropertyStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID>,
        JpaSpecificationExecutor<PropertyEntity> {

    long countByAgencyId(UUID agencyId);

    long countByAgencyIdAndStatus(
            UUID agencyId,
            PropertyStatus status
        );

    List<PropertyEntity> findByAgencyId(UUID agencyId);

    @Modifying
    @Query("UPDATE PropertyEntity p SET p.assignedAgent = null WHERE p.assignedAgent.id = :agentId")
    void unassignAgentFromAllProperties(@Param("agentId") UUID agentId);

    @Query("""
    SELECT new com.realestate.backend.dto.response.PropertySuggestionResponse(p.id, p.title)
    FROM PropertyEntity p
    WHERE p.status = 'ACTIVE'
    AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY p.title
    """)
    List<PropertySuggestionResponse> findMatchingTitles(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT DISTINCT p.city FROM PropertyEntity p
    WHERE p.status = 'ACTIVE'
    AND LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY p.city
    """)
    List<String> findMatchingCities(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT DISTINCT p.district FROM PropertyEntity p
    WHERE p.status = 'ACTIVE'
    AND p.district IS NOT NULL
    AND LOWER(p.district) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY p.district
    """)
    List<String> findMatchingDistricts(@Param("keyword") String keyword, Pageable pageable);

}
