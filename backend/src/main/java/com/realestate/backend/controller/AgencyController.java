package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AgencyAgentFilterRequest;
import com.realestate.backend.dto.response.*;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.request.AgencyFilterRequest;
import com.realestate.backend.dto.request.AgencyPropertyFilterRequest;
import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgencyService;
import com.realestate.backend.service.AppointmentService;
import com.realestate.backend.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;
    private final InquiryService inquiryService;
    private final AppointmentService appointmentService;

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

    @PostMapping(
            value = "/me/logo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'AGENT')")
    @Operation(summary = "Update agency profile logo")
    public ResponseEntity<ApiResponse<AgencyLogoUploadResponse>> uploadAgencyLogo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestPart("file") MultipartFile file
    ) {

        AgencyLogoUploadResponse response = agencyService.uploadLogo(file, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Agency logo uploaded successfully", response)
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
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllProperties(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @ModelAttribute AgencyPropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = agencyService.getMyAgencyProperties(currentUser, filter, pageable);

        ApiResponse<Page<PropertyResponse>> apiResponse =
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

    @GetMapping("/public/{agencyId}/properties")
    @Operation(summary = "Get agency's public properties list")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllAgencies(
            @PathVariable UUID agencyId,
            @ModelAttribute PropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = agencyService.getAgencyProperties(agencyId, filter, pageable);

        ApiResponse<Page<PropertyResponse>> apiResponse =
                ApiResponse.success("Properties list fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @GetMapping("/{agencyId}/agents")
    @Operation(summary = "Get all agents belonging to the specific agency")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAgencyAgents(
            @PathVariable UUID agencyId,
            @ModelAttribute AgencyAgentFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<UserResponse> response = agencyService.getAgencyAgents(agencyId, filter, pageable);

        ApiResponse<Page<UserResponse>> apiResponse =
                ApiResponse.success("Agency's agents fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @DeleteMapping( "/me/logo")
    @Operation(summary = "Remove agency logo")
    public ResponseEntity<ApiResponse<Void>> removeAgencyLogo(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        agencyService.removeAgencyLogo(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Agency logo removed successfully",
                        null
                )
        );

    }

    @GetMapping("/me/inquiries")
    @Operation(summary = "Get current user's agency's inquiries")
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getMyAgencyInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<InquiryResponse> response = inquiryService.getMyAgencyInquiries(currentUser, status, propertyId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry list fetched successfully", response)
        );

    }

    @GetMapping("/agencies/me/appointments")
    @Operation(summary = "Get agency's appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAgencyAppointments (
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<AppointmentResponse> response = appointmentService.getMyAgencyAppointments(currentUser, status, propertyId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment list fetched successfully", response)
        );

    }

}
