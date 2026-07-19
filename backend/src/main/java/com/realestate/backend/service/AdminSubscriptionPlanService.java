package com.realestate.backend.service;

import com.realestate.backend.dto.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.response.AdminSubscriptionPlanResponse;

import java.util.List;
import java.util.UUID;

public interface AdminSubscriptionPlanService {

    AdminSubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest subscriptionPlanEntity);

    List<AdminSubscriptionPlanResponse> getAllSubscriptionPlans(
            AdminSubscriptionPlanFilterRequest filterRequest
    );

    AdminSubscriptionPlanResponse getSubscriptionPlanById(UUID id);

    AdminSubscriptionPlanResponse updateSubscriptionPlan(UUID id, SubscriptionPlanRequest subscriptionPlanEntity);

    void toggleSubscriptionPlanStatus(UUID id);

    String softDeleteSubscriptionPlan(UUID id);

}
