package com.realestate.backend.service.subscription;

import com.realestate.backend.dto.subscription.response.SubscriptionPlanResponse;

import java.util.List;

public interface SubscriptionPlanService {

    List<SubscriptionPlanResponse> getSubscriptionPlans();

}
