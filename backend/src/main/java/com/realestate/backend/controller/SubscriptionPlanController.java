package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.subscription.response.SubscriptionPlanResponse;
import com.realestate.backend.service.subscription.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    @Operation(summary = "Get all active subscription plans publicly")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getSubscriptionPlans() {

        List<SubscriptionPlanResponse> response = subscriptionPlanService.getSubscriptionPlans();

        return ResponseEntity.ok(
                ApiResponse.success("Subscription plans fetched successfully.", response)
        );

    }

}
