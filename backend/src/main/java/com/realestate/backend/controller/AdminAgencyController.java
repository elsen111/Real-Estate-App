package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.request.AgencyStatusRequest;
import com.realestate.backend.dto.response.AdminAgencyResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.service.AdminAgencyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/agencies")
@RequiredArgsConstructor
public class AdminAgencyController {

    private final AdminAgencyService adminAgencyService;

    @GetMapping
    @Operation(summary = "Get all agencies")
    public ResponseEntity<ApiResponse<Page<AdminAgencyResponse>>> getAllAgencies(
            @ModelAttribute AdminAgencyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
            ) {

        Page<AdminAgencyResponse> response = adminAgencyService.getAllAgencies(filter, pageable);

        ApiResponse<Page<AdminAgencyResponse>> apiResponse =
                ApiResponse.success("Agencies fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @GetMapping("/{agencyId}")
    @Operation(summary = "Get agency by id")
    public ResponseEntity<ApiResponse<AdminAgencyResponse>> getAgencyById(
            @PathVariable UUID agencyId
    ) {

        AdminAgencyResponse response = adminAgencyService.getAgencyById(agencyId);

        return ResponseEntity.ok(
                ApiResponse.success("Agency fetched successfully", response)
        );

    }

    @PutMapping("/{agencyId}/status")
    @Operation(summary = "Change agency status")
    public ResponseEntity<ApiResponse<Void>> changeAgencyStatus(
            @PathVariable @NotNull UUID agencyId,
            @Valid @RequestBody AgencyStatusRequest request
    ) {

        String message = adminAgencyService.changeAgencyStatus(agencyId, request.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success(message, null)
        );

    }


    @Transactional
    @DeleteMapping("/{agencyId}")
    @Operation(summary = "Delete a specific agency")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable @NotNull UUID agencyId
    ) {

        String message = adminAgencyService.softDeleteAgency(agencyId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        message, null
                )
        );
    }

    @Transactional
    @PostMapping("/{agencyId}/subscription-plans/{subscriptionId}")
    @Operation(summary = "Assign subscription plan to an agency")
    public ResponseEntity<ApiResponse<AgencySubscriptionResponse>> assignSubscriptionPlan(
            @PathVariable UUID agencyId,
            @PathVariable UUID subscriptionId
    ) {

        AgencySubscriptionResponse response = adminAgencyService.createAgencySubscription(agencyId, subscriptionId);

        return ResponseEntity.ok(ApiResponse.success(
                "Agency assigned successfully", response
        ));

    }

    @GetMapping("/{agencyId}/subscription-plan")
    @Operation(summary = "Get agency subscription")
    public ResponseEntity<ApiResponse<AgencySubscriptionResponse>> getAgencySubscription(
            @PathVariable UUID agencyId
    ) {

        AgencySubscriptionResponse response = adminAgencyService.getAgencySubscription(agencyId);

        return ResponseEntity.ok(ApiResponse.success(
                "Agency subscription fetched successfully", response
        ));

    }

}
