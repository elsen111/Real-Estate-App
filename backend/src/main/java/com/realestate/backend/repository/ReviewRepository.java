package com.realestate.backend.repository;

import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID>, JpaSpecificationExecutor<ReviewEntity> {

    boolean existsByReviewerIdAndPropertyId(UUID reviewerId, UUID propertyId);

    boolean existsByReviewerIdAndAgencyId(UUID reviewerId, UUID agencyId);

    boolean existsByIdAndReviewerId(UUID reviewId, UUID reviewerId);

    Optional<ReviewEntity> findByIdAndReviewerId(UUID reviewId, UUID reviewerId);

    @Query(
            "SELECT COALESCE(SUM(r.rating), 0) " +
                    "FROM ReviewEntity r " +
                    "WHERE r.property.id = :propertyId " +
                    "AND r.status = :status"
    )
    BigDecimal sumRatingByPropertyIdAndStatus(
            @Param("propertyId") UUID propertyId,
            @Param("status") ReviewStatus status
    );

    @Query(
            "SELECT COUNT(r) " +
                    "FROM ReviewEntity r " +
                    "WHERE r.property.id = :propertyId " +
                    "AND r.status = :status"
    )
    Integer countByPropertyIdAndStatus(
            @Param("propertyId") UUID propertyId,
            @Param("status") ReviewStatus status
    );
}
