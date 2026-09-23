package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.AgencyMemberResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgencyMemberService;
import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping("/agents/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENCY_OWNER')")
    @Operation(summary = "Assign agent to agency.")
    public ResponseEntity<ApiResponse<AgencyMemberResponse>> assignAgent(
            @PathVariable UUID agencyId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        AgencyMemberResponse response =
                agencyMemberService.assignAgent(agencyId, userId, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agent assigned successfully", response));
    }
}