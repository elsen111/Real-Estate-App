package com.realestate.backend.service.admin.subscription;

import com.realestate.backend.dto.admin.subscription.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.admin.subscription.request.CreateSubscriptionPlanRequest;
import com.realestate.backend.dto.admin.subscription.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.entity.SubscriptionPlanEntity;

import java.util.List;
import java.util.UUID;

public interface AdminSubscriptionPlanService {

    AdminSubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest subscriptionPlanEntity);

    List<AdminSubscriptionPlanResponse> getAllSubscriptionPlans(
            AdminSubscriptionPlanFilterRequest filterRequest
    );

    AdminSubscriptionPlanResponse getSubscriptionPlanById(UUID id);

}
