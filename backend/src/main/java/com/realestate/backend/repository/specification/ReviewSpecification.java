package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.request.AdminReviewFilterRequest;
import com.realestate.backend.dto.request.PublicReviewFilterRequest;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class ReviewSpecification {

    public ReviewSpecification() {}

    public static Specification<ReviewEntity> withAdminFilter(
            AdminReviewFilterRequest filterRequest
    ) {

        if (filterRequest == null) {
            return Specification.where((Specification<ReviewEntity>) null);
        }

        return Specification
                .where(hasStatus(filterRequest.getStatus()))
                .and(hasTargetType(filterRequest.getTargetType()))
                .and(hasAgencyName(filterRequest.getAgencyName()))
                .and(hasPropertyTitle(filterRequest.getPropertyTitle()))
                .and(hasRating(filterRequest.getRating()));
    }

    public static Specification<ReviewEntity> withPublicFilter(
            UUID agencyId,
            UUID propertyId,
            PublicReviewFilterRequest filterRequest
    ) {

        Specification<ReviewEntity> spec = Specification.where(hasStatus(ReviewStatus.APPROVED));

        if (filterRequest != null) {
            spec = spec.and(hasRating(filterRequest.rating()));
        }

        return spec.and(hasAgencyId(agencyId))
                .and(hasPropertyId(propertyId));
    }


    //    HELPER METHODS
    private static Specification<ReviewEntity> hasStatus(ReviewStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    private static Specification<ReviewEntity> hasTargetType(ReviewTargetType targetType) {
        return (root, query, criteriaBuilder) ->
                targetType == null ? null : criteriaBuilder.equal(root.get("target"), targetType);
    }

    private static Specification<ReviewEntity> hasAgencyName(String agencyName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(agencyName)) {
                return null;
            }

            var agencyJoin = root.join("agency", JoinType.LEFT);
            String pattern = "%" + agencyName.trim().toLowerCase() + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(agencyJoin.get("name")),
                    pattern
            );
        };
    }

    private static Specification<ReviewEntity> hasPropertyTitle(String propertyTitle) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(propertyTitle)) {
                return null;
            }

            var propertyJoin = root.join("property", JoinType.LEFT);
            String pattern = "%" + propertyTitle.trim().toLowerCase() + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(propertyJoin.get("title")),
                    pattern
            );
        };
    }

    private static Specification<ReviewEntity> hasRating(Integer rating) {
        return (
                (root, query, criteriaBuilder) ->
                        rating == null ? null
                        : criteriaBuilder.equal(root.get("rating"), rating)
                );
    }

    private static Specification<ReviewEntity> hasAgencyId(UUID agencyId) {
        return (
                (root, query, criteriaBuilder) ->
                        agencyId == null ? null : criteriaBuilder.equal(root.join("agency").get("id"), agencyId)
                );
    }

    private static Specification<ReviewEntity> hasPropertyId(UUID propertyId) {
        return (
                (root, query, criteriaBuilder) ->
                        propertyId == null ? null : criteriaBuilder.equal(root.join("property").get("id"), propertyId)
        );
    }

}
