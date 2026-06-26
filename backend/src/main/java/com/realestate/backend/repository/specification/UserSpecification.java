package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.admin.user.request.AdminUserFilterRequest;
import com.realestate.backend.entity.UserEntity;
import org.springframework.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<UserEntity> withFilter(
            AdminUserFilterRequest filterRequest
    ) {

        if(filterRequest == null) {
            Specification.where((Specification<Object>) null);
        };

        assert filterRequest != null;
        return Specification.where(hasRole(filterRequest.getRole()))
                .and(isEnabled(filterRequest.getEnabled()))
                .and(hasQuery(filterRequest.getQuery()));

    }

    private static Specification<UserEntity> hasRole(Object role) {
        return ((root, query, criteriaBuilder) -> role == null ? null : criteriaBuilder.equal(root.get("role"), role));
    }

    private static Specification<UserEntity> isEnabled(Boolean enabled) {
        return ((root, query, criteriaBuilder) -> enabled == null ? null : criteriaBuilder.equal(root.get("enabled"), enabled));
    }

    private static Specification<UserEntity> hasQuery(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return null;
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern)
            );

        };
    }

}
