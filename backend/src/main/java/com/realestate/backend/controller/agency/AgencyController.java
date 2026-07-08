package com.realestate.backend.controller.agency;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.agency.request.UpdateAgencyRequest;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.agency.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

}
