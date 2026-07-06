package com.realestate.backend.service.admin.subscription;

import com.realestate.backend.dto.admin.subscription.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.admin.subscription.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.admin.subscription.response.AdminSubscriptionPlanResponse;

import java.util.List;
import java.util.UUID;

public interface AdminSubscriptionPlanService {

    AdminSubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest subscriptionPlanEntity);

    List<AdminSubscriptionPlanResponse> getAllSubscriptionPlans(
            AdminSubscriptionPlanFilterRequest filterRequest
    );

    AdminSubscriptionPlanResponse getSubscriptionPlanById(UUID id);

    AdminSubscriptionPlanResponse updateSubscriptionPlan(UUID id, SubscriptionPlanRequest subscriptionPlanEntity);

}
