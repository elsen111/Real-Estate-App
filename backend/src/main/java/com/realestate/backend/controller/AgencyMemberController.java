package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.agency.request.AgencyMemberAssignmentRequest;
import com.realestate.backend.dto.agency.response.AgencyMemberResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.agency.AgencyMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/agencies/{agencyId}/members")
@RequiredArgsConstructor
public class AgencyMemberController {

    private final AgencyMemberService agencyMemberService;

    @PostMapping("/admins")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENCY_ADMIN', 'AGENCY_OWNER')")
    public ResponseEntity<ApiResponse<AgencyMemberResponse>> assignAgencyAdmin(
            @PathVariable UUID agencyId,
            @Valid @RequestBody AgencyMemberAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        AgencyMemberResponse response =
                agencyMemberService.assignAgencyAdmin(agencyId, request, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agency admin assigned successfully", response));
    }

    @PostMapping("/agents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENCY_ADMIN')")
    public ResponseEntity<ApiResponse<AgencyMemberResponse>> assignAgent(
            @PathVariable UUID agencyId,
            @Valid @RequestBody AgencyMemberAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        AgencyMemberResponse response =
                agencyMemberService.assignAgent(agencyId, request, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agent assigned successfully", response));
    }
}