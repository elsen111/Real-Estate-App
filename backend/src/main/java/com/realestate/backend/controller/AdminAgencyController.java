package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.request.AgencyStatusRequest;
import com.realestate.backend.dto.request.InquiryFilterRequest;
import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.dto.response.AdminAgencyResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.service.AdminAgencyService;
import com.realestate.backend.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/agencies")
@RequiredArgsConstructor
@Tag(
        name = "Admin - Agency Management",
        description = "Administrative endpoints for managing agencies, including agency information, approval workflows, subscriptions, and inquiries."
)
public class AdminAgencyController {

    private final AdminAgencyService adminAgencyService;
    private final InquiryService inquiryService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping
    @Operation(
            summary = "Get all agencies",
            description = "Retrieves a paginated list of agencies with optional filtering by agency attributes and status. Supports sorting and pagination."
    )
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

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping("/{agencyId}")
    @Operation(
            summary = "Get agency by ID",
            description = "Retrieves detailed information about a specific agency using its unique identifier."
    )
    public ResponseEntity<ApiResponse<AdminAgencyResponse>> getAgencyById(
            @PathVariable UUID agencyId
    ) {

        AdminAgencyResponse response = adminAgencyService.getAgencyById(agencyId);

        return ResponseEntity.ok(
                ApiResponse.success("Agency fetched successfully", response)
        );

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PutMapping("/{agencyId}/status")
    @Operation(
            summary = "Change agency status",
            description = "Updates the status of a specific agency. Allows administrators to change the agency's current status according to the provided request."
    )
    public ResponseEntity<ApiResponse<Void>> changeAgencyStatus(
            @PathVariable UUID agencyId,
            @Valid @RequestBody AgencyStatusRequest request
    ) {

        String message = adminAgencyService.changeAgencyStatus(agencyId, request.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success(message, null)
        );

    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    @DeleteMapping("/{agencyId}")
    @Operation(
            summary = "Delete an agency",
            description = "Soft-deletes a specific agency using its unique identifier. This operation is restricted to super administrators."
    )
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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    @PostMapping("/{agencyId}/subscription-plans/{subscriptionId}")
    @Operation(
            summary = "Assign subscription plan to agency",
            description = "Assigns a subscription plan to a specific agency by creating an agency subscription using the provided agency and subscription plan identifiers."
    )
    public ResponseEntity<ApiResponse<AgencySubscriptionResponse>> assignSubscriptionPlan(
            @PathVariable UUID agencyId,
            @PathVariable UUID subscriptionId
    ) {

        AgencySubscriptionResponse response = adminAgencyService.createAgencySubscription(agencyId, subscriptionId);

        return ResponseEntity.ok(ApiResponse.success(
                "Agency assigned successfully", response
        ));

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping("/{agencyId}/subscription-plan")
    @Operation(
            summary = "Get agency subscription",
            description = "Retrieves the current subscription information associated with a specific agency, including its assigned subscription plan and subscription details."
    )
    public ResponseEntity<ApiResponse<AgencySubscriptionResponse>> getAgencySubscription(
            @PathVariable UUID agencyId
    ) {

        AgencySubscriptionResponse response = adminAgencyService.getAgencySubscription(agencyId);

        return ResponseEntity.ok(ApiResponse.success(
                "Agency subscription fetched successfully", response
        ));

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PutMapping("/{agencyId}")
    @Operation(
            summary = "Approve agency",
            description = "Approves a pending agency registration request, allowing the agency to become eligible to operate on the platform."
    )
    public ResponseEntity<ApiResponse<AdminAgencyResponse>> updateAgency(
            @PathVariable UUID agencyId,
            @Valid @RequestBody UpdateAgencyRequest request
    ) {

        AdminAgencyResponse response = adminAgencyService.updateAgency(agencyId, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Agency information updated successfully", response
        ));

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PutMapping("/{agencyId}/approve")
    @Operation(
            summary = "Approve agency",
            description = "Approves a pending agency registration request, allowing the agency to become eligible to operate on the platform."
    )
    public ResponseEntity<ApiResponse<Void>> approveAgency(
            @PathVariable UUID agencyId
    ) {

        String message = adminAgencyService.approveAgency(agencyId);

        return ResponseEntity.ok(ApiResponse.success(
                message, null
        ));

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PutMapping("/{agencyId}/reject")
    @Operation(
            summary = "Reject agency",
            description = "Rejects a pending agency registration request and updates the agency's status accordingly."
    )
    public ResponseEntity<ApiResponse<Void>> rejectAgency(
            @PathVariable UUID agencyId
    ) {

        String message = adminAgencyService.rejectAgency(agencyId);

        return ResponseEntity.ok(ApiResponse.success(
                message, null
        ));

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping("/{agencyId}/inquiries")
    @Operation(
            summary = "Get agency inquiries",
            description = "Retrieves a paginated list of inquiries associated with a specific agency. Supports optional filtering, sorting, and pagination."
    )
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getAgencyInquiries(
            @PathVariable UUID agencyId,
            @ModelAttribute InquiryFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<InquiryResponse> response = inquiryService.getAgencyInquiriesById(agencyId, filter, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Agency inquiries fetched successfully", response
        ));

    }

}
