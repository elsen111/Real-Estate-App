package com.realestate.backend.controller.agency;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.admin.property.request.AdminPropertyFilterRequest;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import com.realestate.backend.dto.agency.request.AgencyFilterRequest;
import com.realestate.backend.dto.agency.request.AgencyPropertyFilterRequest;
import com.realestate.backend.dto.agency.request.UpdateAgencyRequest;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.agency.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    @GetMapping("/me")
    @Operation(summary = "Get current agency information.")
    public ResponseEntity<ApiResponse<AgencyResponse>> getCurrentAgency(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        AgencyResponse response = agencyService.getCurrentAgency(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Current agency information fetched successfully", response)
        );

    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(summary = "Update agency profile")
    public ResponseEntity<ApiResponse<AgencyResponse>> updateOwnAgency(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateAgencyRequest request
            ) {

        AgencyResponse response = agencyService.updateOwnAgency(currentUser, request);

        return ResponseEntity.ok(
                ApiResponse.success("Agency information updated successfully", response)
        );

    }

    @GetMapping("/me/subscription")
    @Operation(summary = "Get current agency's subscription.")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'AGENT')")
    public ResponseEntity<ApiResponse<AgencySubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        AgencySubscriptionResponse response = agencyService.getMySubscription(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription information fetched successfully", response)
        );

    }

    @GetMapping("/me/properties")
    @Operation(summary = "Get all properties belonging to the current agency")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'AGENT')")
    public ResponseEntity<ApiResponse<Page<AdminPropertyResponse>>> getAllProperties(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @ModelAttribute AgencyPropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<AdminPropertyResponse> response = agencyService.getMyAgencyProperties(currentUser, filter, pageable);

        ApiResponse<Page<AdminPropertyResponse>> apiResponse =
                ApiResponse.success("Properties fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @GetMapping("/public")
    @Operation(summary = "Get all public agencies")
    public ResponseEntity<ApiResponse<Page<AgencyResponse>>> getAllAgencies(
            @ModelAttribute AgencyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<AgencyResponse> response = agencyService.getAllPublicAgencies(filter, pageable);

        ApiResponse<Page<AgencyResponse>> apiResponse =
                ApiResponse.success("Agencies fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }


    @GetMapping("/public/{agencyId}")
    @Operation(summary = "Get specific agency's public information.")
    public ResponseEntity<ApiResponse<AgencyResponse>> getAgencyPublicInfo(
            @PathVariable UUID agencyId
    ){

        AgencyResponse response = agencyService.getPublicAgencyInfo(agencyId);

        return ResponseEntity.ok(
                ApiResponse.success("Agency information fetched successfully", response)
        );

    }

}
