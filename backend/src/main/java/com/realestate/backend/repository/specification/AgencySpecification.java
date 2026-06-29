package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.user.request.AdminUserFilterRequest;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class AgencySpecification {

    public AgencySpecification() {}

    public static Specification<AgencyEntity> withFilter(
            AdminAgencyFilterRequest filterRequest
    ) {

        if(filterRequest == null) {
            Specification.where((Specification<Object>) null);
        };

        assert filterRequest != null;

        return Specification.where(hasCity(filterRequest.getCity()))
                .and(hasEmail(filterRequest.getEmail()))
                .and(isEnabled(filterRequest.getStatus()))
                .and(hasQuery(filterRequest.getQuery()));

    }

    private static Specification<AgencyEntity> hasCity(Object city) {
        return ((root, query, criteriaBuilder) -> city == null ? null : criteriaBuilder.equal(root.get("city"), city));
    }

    private static Specification<AgencyEntity> hasEmail(Object email) {
        return ((root, query, criteriaBuilder) -> email == null ? null : criteriaBuilder.equal(root.get("email"), email));
    }

    private static Specification<AgencyEntity> isEnabled(Boolean status) {
        return ((root, query, criteriaBuilder) -> status == null ? null : criteriaBuilder.equal(root.get("status"), status));
    }

    private static Specification<AgencyEntity> hasQuery(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return null;
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email").as(String.class)), pattern)
            );

        };
    }

}
