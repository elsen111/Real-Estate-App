package com.realestate.backend.service;

import com.realestate.backend.dto.response.SubscriptionPlanResponse;

import java.util.List;

public interface SubscriptionPlanService {

    List<SubscriptionPlanResponse> getSubscriptionPlans();

}
