package com.realestate.backend.controller.admin;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.admin.subscription.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.admin.subscription.request.CreateSubscriptionPlanRequest;
import com.realestate.backend.dto.admin.subscription.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.service.admin.subscription.AdminSubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/subscription-plans")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionPlanService adminSubscriptionPlanService;

    @PostMapping
    @Operation(summary = "Create a new subscription plan")
    public ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> createSubscriptionPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request
    ) {

        AdminSubscriptionPlanResponse response = adminSubscriptionPlanService.createSubscriptionPlan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription plan created successfully", response));

    }

    @GetMapping
    @Operation(summary = "Get all subscription plans")
    public ResponseEntity<ApiResponse<List<AdminSubscriptionPlanResponse>>> getAllSubscriptionPlans(
            @ModelAttribute AdminSubscriptionPlanFilterRequest request
            ){
        List<AdminSubscriptionPlanResponse> response = adminSubscriptionPlanService.getAllSubscriptionPlans(request);

        ApiResponse<List<AdminSubscriptionPlanResponse>> apiResponse =
                ApiResponse.success("Subscription plans fetched successfully", response);

        return ResponseEntity.ok(apiResponse);
    }


}
