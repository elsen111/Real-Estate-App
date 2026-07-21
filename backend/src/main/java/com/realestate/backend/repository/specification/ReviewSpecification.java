package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.request.AdminReviewFilterRequest;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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
                .and(hasPropertyTitle(filterRequest.getPropertyTitle()));
    }

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

}
