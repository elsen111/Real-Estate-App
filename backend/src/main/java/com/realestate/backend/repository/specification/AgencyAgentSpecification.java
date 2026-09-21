package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.request.AgencyAgentFilterRequest;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.enums.Role;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class AgencyAgentSpecification {

    private AgencyAgentSpecification() {}

    public static Specification<AgencyMemberEntity> withAgencyAgentFilter(
            UUID agencyId,
            AgencyAgentFilterRequest filterRequest
    ) {
        Specification<AgencyMemberEntity> spec = Specification
                .where(hasAgencyId(agencyId))
                .and(hasRole(Role.AGENT));

        if (filterRequest == null) {
            return spec;
        }

        return spec
                .and(hasQuery(filterRequest.getQuery()));
    }



//    HELPER METHODS
    private static Specification<AgencyMemberEntity> hasAgencyId(UUID agencyId) {
        return (root, query, cb) -> cb.equal(root.join("agency").get("id"), agencyId);
    }

    private static Specification<AgencyMemberEntity> hasRole(Role role) {
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    private static Specification<AgencyMemberEntity> hasQuery(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return null;
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.join("user").get("fullName")), pattern),
                    cb.like(cb.lower(root.join("user").get("email")), pattern)
            );
        };
    }

}
