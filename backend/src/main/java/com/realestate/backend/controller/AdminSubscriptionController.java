package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.service.AdminSubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/subscription-plans")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionPlanService adminSubscriptionPlanService;

    @PostMapping
    @Operation(summary = "Create a new subscription plan")
    public ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> createSubscriptionPlan(
            @Valid @RequestBody SubscriptionPlanRequest request
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

    @GetMapping("/{planId}")
    @Operation(summary = "Get subscription plan by id")
    public ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> getSubscriptionPlanById(
            @PathVariable UUID planId
    ) {
        AdminSubscriptionPlanResponse response = adminSubscriptionPlanService.getSubscriptionPlanById(planId);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription plan fetched successfully", response)
        );
    }

    @PutMapping("/{planId}")
    @Operation(summary = "Update subscription plan")
    public ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> updateSubscriptionPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {

        AdminSubscriptionPlanResponse response = adminSubscriptionPlanService.updateSubscriptionPlan(planId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription plan updated successfully", response)
        );

    }

    @PatchMapping("/{planId}/status")
    @Operation(summary = "Change subscription plan status")
    public ResponseEntity<ApiResponse<Void>> toggleSubscriptionPlanStatus(
            @PathVariable UUID planId
    ) {

        adminSubscriptionPlanService.toggleSubscriptionPlanStatus(planId);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription plan updated successfully")
        );

    }

    @Transactional
    @DeleteMapping("/{planId}")
    @Operation(summary = "Delete a subscription plan")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriptionPlan(
            @PathVariable UUID planId
    ) {

        String message = adminSubscriptionPlanService.softDeleteSubscriptionPlan(planId);

        return ResponseEntity.ok(
                ApiResponse.success(message)
        );

    }


}
