package com.realestate.backend.repository;

import com.realestate.backend.dto.response.PopularCityProjection;
import com.realestate.backend.dto.response.PropertySuggestionResponse;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.PropertyStatus;
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

//    @Query("""
//        SELECT p.city AS cityName,
//               COUNT(p) AS propertyCount,
//               COALESCE(SUM(p.viewCount), 0) AS totalViews
//        FROM PropertyEntity p
//        WHERE p.status = :status
//          AND p.city IS NOT NULL
//          AND TRIM(p.city) <> ''
//        GROUP BY p.city
//        ORDER BY SUM(p.viewCount) DESC, COUNT(p) DESC
//        """)
//    List<PopularCityProjection> findPopularCities(
//            @Param("status") PropertyStatus status,
//            Pageable pageable
//    );

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
    Page<PopularCityProjection> findPopularCities(
            @Param("status") PropertyStatus status,
            Pageable pageable
    );

}
