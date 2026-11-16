package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.SubscriptionPlanResponse;
import com.realestate.backend.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
@Tag(
        name = "Subscription Plans",
        description = """
                Endpoints for retrieving available subscription plans.
                Provides information about active plans and their associated features and limits.
                """
)
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    @Operation(
            summary = "Get all active subscription plans",
            description = """
                    Retrieves a list of all currently active subscription plans.
                    Returns plan information, including available features, pricing, and subscription limits
                    as defined by the subscription plan response.
                    This endpoint does not require authentication.
                    """
    )
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getSubscriptionPlans() {

        List<SubscriptionPlanResponse> response = subscriptionPlanService.getSubscriptionPlans();

        return ResponseEntity.ok(
                ApiResponse.success("Subscription plans fetched successfully.", response)
        );

    }

}
