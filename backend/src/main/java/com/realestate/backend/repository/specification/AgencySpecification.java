package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.request.AgencyFilterRequest;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.enums.AgencyStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class AgencySpecification {

    public AgencySpecification() {}

    public static Specification<AgencyEntity> withPublicFilter(
            AgencyFilterRequest filterRequest
    ) {

        Specification<AgencyEntity> spec = Specification
                .where(isNotDeleted())
                .and(hasStatus(AgencyStatus.APPROVED));

        if (filterRequest == null) {
            return spec;
        }

        return spec
                .and(hasCity(filterRequest.getCity()))
                .and(hasQuery(filterRequest.getQuery()));
    }



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
                .and(isDeleted(filterRequest.getIsDeleted()))
                .and(hasQuery(filterRequest.getQuery()));

    }



//    HELPER METHODS
    private static Specification<AgencyEntity> hasCity(Object city) {
        return ((root, query, criteriaBuilder) -> city == null ? null : criteriaBuilder.equal(root.get("city"), city));
    }

    private static Specification<AgencyEntity> hasEmail(Object email) {
        return ((root, query, criteriaBuilder) -> email == null ? null : criteriaBuilder.equal(root.get("email"), email));
    }

    private static Specification<AgencyEntity> isEnabled(Boolean status) {
        return ((root, query, criteriaBuilder) -> status == null ? null : criteriaBuilder.equal(root.get("status"), status));
    }

    private static Specification<AgencyEntity> isDeleted(Boolean isDeleted) {
        return ((root, query, criteriaBuilder) -> isDeleted == null ? null : criteriaBuilder.equal(root.get("isDeleted"), isDeleted));
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

    private static Specification<AgencyEntity> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("isDeleted"));
    }

    private static Specification<AgencyEntity> hasStatus(AgencyStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

}
