package com.realestate.backend.repository;

import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID>, JpaSpecificationExecutor<ReviewEntity> {

    boolean existsByReviewerIdAndPropertyId(UUID reviewerId, UUID propertyId);

    boolean existsByReviewerIdAndAgencyId(UUID reviewerId, UUID agencyId);

    boolean existsByIdAndReviewerId(UUID reviewId, UUID reviewerId);

    Optional<ReviewEntity> findByIdAndReviewerId(UUID reviewId, UUID reviewerId);
}
