package com.realestate.backend.repository;

import com.realestate.backend.dto.response.PopularCityResponse;
import com.realestate.backend.dto.response.PropertySuggestionResponse;
import com.realestate.backend.dto.response.TopAgencyResponse;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID>,
        JpaSpecificationExecutor<PropertyEntity> {

    long countByAgencyId(
            UUID agencyId
    );

    long countByAgencyIdAndStatus(
            UUID agencyId,
            PropertyStatus status
    );

    List<PropertyEntity> findByAgencyId(
            UUID agencyId
    );

    @Modifying
    @Query("UPDATE PropertyEntity p SET p.assignedAgent = null WHERE p.assignedAgent.id = :agentId")
    void unassignAgentFromAllProperties(
            @Param("agentId") UUID agentId
    );

    @Query(
            """
                    SELECT new com.realestate.backend.dto.response.PropertySuggestionResponse(p.id, p.title)
                    FROM PropertyEntity p
                    WHERE p.status = 'ACTIVE'
                    AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    ORDER BY p.title
                    """
    )
    List<PropertySuggestionResponse> findMatchingTitles(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(
            """
                    SELECT DISTINCT p.city FROM PropertyEntity p
                    WHERE p.status = 'ACTIVE'
                    AND LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    ORDER BY p.city
                    """
    )
    List<String> findMatchingCities(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(
            """
                    SELECT DISTINCT p.district FROM PropertyEntity p
                    WHERE p.status = 'ACTIVE'
                    AND p.district IS NOT NULL
                    AND LOWER(p.district) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    ORDER BY p.district
                    """
    )
    List<String> findMatchingDistricts(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    boolean existsByCategoryIdAndStatus(
            UUID categoryId,
            PropertyStatus status
    );

    boolean existsByAgencyIdAndStatus(
            UUID id,
            PropertyStatus status
    );

    long countByAgencyIdAndStatusIn(
            UUID id,
            Collection<PropertyStatus> propertyStatus
    );

    @Query("""
        SELECT p.city AS cityName,
               COUNT(p) AS propertyCount,
               COALESCE(SUM(p.viewCount), 0) AS totalViews
        FROM PropertyEntity p
        WHERE p.status = :status
          AND p.city IS NOT NULL
          AND TRIM(p.city) <> ''
        GROUP BY p.city
        ORDER BY SUM(p.viewCount) DESC, COUNT(p) DESC, p.city ASC
        """)
    Page<PopularCityResponse> findPopularCities(
            @Param("status") PropertyStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT new com.realestate.backend.dto.response.TopAgencyResponse(
                a.id, 
                a.name, 
                a.city,
                f.fileUrl, 
                COUNT(DISTINCT p.id), 
                CAST(COALESCE(SUM(DISTINCT p.viewCount), 0) AS Long),
                CAST(AVG(CAST(r.rating AS Double)) AS Double)
            )
            FROM AgencyEntity a
            LEFT JOIN a.logoUrl am
            LEFT JOIN am.media f
            LEFT JOIN PropertyEntity p ON p.agency.id = a.id AND p.status = :propStatus
            LEFT JOIN ReviewEntity r ON r.agency.id = a.id AND r.status = :reviewStatus
            WHERE a.status = :agencyStatus 
            AND a.isDeleted = false
            GROUP BY a.id, a.name, f.fileUrl
            HAVING COUNT(DISTINCT p.id) > 0 OR AVG(r.rating) IS NOT NULL
            ORDER BY COALESCE(AVG(CAST(r.rating AS Double)), 0.0) DESC, COALESCE(SUM(DISTINCT p.viewCount), 0) DESC, COUNT(DISTINCT p.id) DESC
            """)
    Page<TopAgencyResponse> findTopAgencies(
            @Param("agencyStatus") AgencyStatus agencyStatus,
            @Param("propStatus") PropertyStatus propStatus,
            @Param("reviewStatus") ReviewStatus reviewStatus,
            Pageable pageable
    );


}
