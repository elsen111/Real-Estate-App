package com.realestate.backend.service.admin.subscription;

import com.realestate.backend.dto.admin.subscription.request.CreateSubscriptionPlanRequest;
import com.realestate.backend.dto.admin.subscription.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.entity.SubscriptionPlanEntity;

public interface AdminSubscriptionPlanService {

    public AdminSubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest subscriptionPlanEntity);

}
