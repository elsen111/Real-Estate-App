package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.request.AgencyAgentFilterRequest;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class AgencyAgentSpecification {

    private AgencyAgentSpecification() {}

    public static Specification<UserEntity> withAgencyAgentFilter(
            UUID agencyId,
            AgencyAgentFilterRequest filterRequest
    ) {
        Specification<UserEntity> spec = Specification
                .where(hasAgencyId(agencyId))
                .and(hasRole(Role.AGENT));

        if (filterRequest == null) {
            return spec;
        }

        return spec
                .and(isEnabled(filterRequest.getEnabled()))
                .and(hasQuery(filterRequest.getQuery()));
    }



//    HELPER METHODS
    private static Specification<UserEntity> hasAgencyId(UUID agencyId) {
        return (root, query, cb) -> cb.equal(root.get("agency").get("id"), agencyId);
    }

    private static Specification<UserEntity> hasRole(Role role) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<UserEntity, RoleEntity> roleJoin = root.join("roles");
            return cb.equal(roleJoin.get("roleName"), role);
        };
    }

    private static Specification<UserEntity> isEnabled(Boolean enabled) {
        return (root, query, cb) -> enabled == null ? null : cb.equal(root.get("enabled"), enabled);
    }

    private static Specification<UserEntity> hasQuery(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return null;
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }

}
