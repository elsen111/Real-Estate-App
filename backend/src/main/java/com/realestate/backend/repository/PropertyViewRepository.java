package com.realestate.backend.repository;

import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.PropertyViewEntity;
import com.realestate.backend.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(
            """
                    SELECT pv.property
                    FROM PropertyViewEntity pv
                    WHERE pv.viewedAt >= :from
                      AND pv.property.status = :status
                    GROUP BY pv.property
                    ORDER BY COUNT(pv.id) DESC
                    """
    )
    Page<PropertyEntity> findPopularProperties(
            @Param("from") LocalDateTime from,
            @Param("status") PropertyStatus status,
            Pageable pageable
    );

}
