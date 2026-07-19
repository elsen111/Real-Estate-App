package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import org.springframework.data.jpa.domain.Specification;

public class AdminSubscriptionPlanSpecification {

    public AdminSubscriptionPlanSpecification() {}

    public static Specification<SubscriptionPlanEntity> withFilter(
            AdminSubscriptionPlanFilterRequest filterRequest
    ) {

        if (filterRequest == null) {
            Specification.where((Specification<Object>) null);
        }

        assert filterRequest != null;

        return Specification.where(isActive(filterRequest.getActive()));

    }



//    HELPER METHODS
    private static Specification<SubscriptionPlanEntity> isActive(Boolean isActive) {
        return ((root, query, criteriaBuilder) -> isActive == null ? null : criteriaBuilder.equal(root.get("active"), isActive));
    }

}
