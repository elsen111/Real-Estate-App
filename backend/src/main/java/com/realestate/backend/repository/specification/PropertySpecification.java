package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.agency.request.AgencyFilterRequest;
import com.realestate.backend.dto.property.request.PropertyFilterRequest;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.enums.PropertyStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class PropertySpecification {

    public PropertySpecification() {}

    public static Specification<PropertyEntity> withFilter(
            PropertyFilterRequest filterRequest
    ) {

        if(filterRequest == null) {
            Specification.where((Specification<Object>) null);
        };

        assert filterRequest != null;

        return Specification.where(hasCity(filterRequest.getCity()))
                .and(hasCity(filterRequest.getCity()))
                .and(hasStatus(filterRequest.getStatus()))
                .and(hasAgencyName(filterRequest.getAgencyName()))
                .and(isFeatured(filterRequest.getFeatured()))
                .and(hasQuery(filterRequest.getQuery()));

    }

    public static Specification<PropertyEntity> withPublicFilter(
            PropertyFilterRequest filterRequest
    ) {

        Specification<PropertyEntity> spec = Specification
                .where(hasStatus(PropertyStatus.ACTIVE));

        if (filterRequest == null) {
            return spec;
        }

        return spec
                .and(hasCity(filterRequest.getCity()))
                .and(hasAgencyName(filterRequest.getAgencyName()))
                .and(isFeatured(filterRequest.getFeatured()))
                .and(hasQuery(filterRequest.getQuery()));
    }

    private static Specification<PropertyEntity> hasCity(Object city) {
        return ((root, query, criteriaBuilder) -> city == null ? null : criteriaBuilder.equal(root.get("city"), city));
    }

    private static Specification<PropertyEntity> hasAgencyName(String agencyName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(agencyName)) {
                return null;
            }
            Join<PropertyEntity, AgencyEntity> agencyJoin = root.join("agency", JoinType.INNER);
            return cb.like(cb.lower(agencyJoin.get("name")), "%" + agencyName.trim().toLowerCase() + "%");
        };
    }

    private static Specification<PropertyEntity> hasStatus(PropertyStatus status) {
        return ((root, query, criteriaBuilder) -> status == null ? null : criteriaBuilder.equal(root.get("status"), status));
    }

    private static Specification<PropertyEntity> isFeatured(Boolean isFeatured) {
        return ((root, query, criteriaBuilder) -> isFeatured == null ? null : criteriaBuilder.equal(root.get("featured"), isFeatured));
    }

    private static Specification<PropertyEntity> hasQuery(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return null;
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("district").as(String.class)), pattern)
            );

        };
    }

}
