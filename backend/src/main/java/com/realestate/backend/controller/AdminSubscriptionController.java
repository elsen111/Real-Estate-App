package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.service.AdminSubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/subscription-plans")
@RequiredArgsConstructor
@Tag(
        name = "Admin - Subscription Plan Management",
        description = """
                Administrative endpoints for managing subscription plans, including plan creation,
                retrieval, updates, activation, deactivation, and deletion.
                """
)
public class AdminSubscriptionController {

    private final AdminSubscriptionPlanService adminSubscriptionPlanService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create a subscription plan",
            description = """
                    Creates a new subscription plan using the provided configuration.
                    Requires super administrator privileges.
                    """
    )
    public ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> createSubscriptionPlan(
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {

        AdminSubscriptionPlanResponse response = adminSubscriptionPlanService.createSubscriptionPlan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription plan created successfully", response));

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping
    @Operation(
            summary = "Get all subscription plans",
            description = """
                    Retrieves a list of subscription plans with optional filtering by plan attributes
                    and status. Accessible to administrators and super administrators.
                    """
    )
    public ResponseEntity<ApiResponse<List<AdminSubscriptionPlanResponse>>> getAllSubscriptionPlans(
            @ModelAttribute AdminSubscriptionPlanFilterRequest request
    ) {
        List<AdminSubscriptionPlanResponse> response = adminSubscriptionPlanService.getAllSubscriptionPlans(request);

        ApiResponse<List<AdminSubscriptionPlanResponse>> apiResponse =
                ApiResponse.success("Subscription plans fetched successfully", response);

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{planId}")
    @Operation(
            summary = "Get subscription plan by ID",
            description = "Retrieves detailed information about a specific subscription plan using its unique identifier."
    )
    public ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> getSubscriptionPlanById(
            @PathVariable UUID planId
    ) {
        AdminSubscriptionPlanResponse response = adminSubscriptionPlanService.getSubscriptionPlanById(planId);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription plan fetched successfully", response)
        );
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{planId}")
    @Operation(
            summary = "Update subscription plan",
            description = """
                    Updates the configuration and details of an existing subscription plan
                    using the provided request data.
                    Requires super administrator privileges.
                    """
    )
    public ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> updateSubscriptionPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {

        AdminSubscriptionPlanResponse response = adminSubscriptionPlanService.updateSubscriptionPlan(planId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription plan updated successfully", response)
        );

    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{planId}/status")
    @Operation(
            summary = "Toggle subscription plan status",
            description = """
                    Toggles the active status of an existing subscription plan.
                    Changes an active plan to inactive or an inactive plan to active.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> toggleSubscriptionPlanStatus(
            @PathVariable UUID planId
    ) {

        adminSubscriptionPlanService.toggleSubscriptionPlanStatus(planId);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription plan updated successfully")
        );

    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    @DeleteMapping("/{planId}")
    @Operation(
            summary = "Delete subscription plan",
            description = """
                    Soft-deletes an existing subscription plan using its unique identifier.
                    The plan is marked as deleted without permanently removing its database record.
                    Requires super administrator privileges.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> deleteSubscriptionPlan(
            @PathVariable UUID planId
    ) {

        String message = adminSubscriptionPlanService.softDeleteSubscriptionPlan(planId);

        return ResponseEntity.ok(
                ApiResponse.success(message)
        );

    }


}
