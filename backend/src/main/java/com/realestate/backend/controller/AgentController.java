package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.AgentResponse;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get agent information.")
    public ResponseEntity<ApiResponse<AgentResponse>> getAgencyPublicInfo(
            @PathVariable UUID userId
    ){

         AgentResponse response = agentService.getAgentByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Agent information fetched successfully", response)
        );

    }

    @GetMapping("/{userId}/properties")
    @Operation(summary = "Get agent's assigned properties")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllAgencies(
            @PathVariable UUID userId,
            @ModelAttribute PropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = agentService.getAgentProperties(userId, filter, pageable);

        ApiResponse<Page<PropertyResponse>> apiResponse =
                ApiResponse.success("Agent's properties fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @DeleteMapping("/{agentId}")
    @Operation(summary = "Delete an agent from their agency")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENCY_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteAgent(
            @PathVariable UUID agentId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        agentService.deleteAgentFromAgency(agentId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Agent removed successfully", null)
        );
    }

}
