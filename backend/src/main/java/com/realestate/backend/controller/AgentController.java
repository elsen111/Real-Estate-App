package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.InquiryFilterRequest;
import com.realestate.backend.dto.response.AgentResponse;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Agents",
        description = """
                APIs for retrieving public agent information, browsing agents' assigned properties,
                managing agency agents, and accessing agent-specific properties and inquiries.
                """
)
public class AgentController {

    private final AgentService agentService;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get public agent information",
            description = """
                    Retrieves publicly available information about an agent using their user ID.
                    This endpoint is accessible without authentication.
                    """
    )
    public ResponseEntity<ApiResponse<AgentResponse>> getAgentPublicInfo(
            @PathVariable UUID userId
    ) {

        AgentResponse response = agentService.getAgentByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Agent information fetched successfully", response)
        );

    }

    //    Public endpoint
    @GetMapping("/{userId}/properties")
    @Operation(
            summary = "Get agent's public properties",
            description = """
                    Retrieves a paginated list of publicly available properties assigned to the specified agent.
                    Supports property filtering and pagination.
                    This endpoint is accessible without authentication.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAgentProperties(
            @PathVariable UUID userId,
            @ModelAttribute PropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = agentService.getPublicAgentProperties(userId, filter, pageable);

        ApiResponse<Page<PropertyResponse>> apiResponse =
                ApiResponse.success("Agent's properties fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @DeleteMapping("/{agentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENCY_OWNER')")
    @Operation(
            summary = "Remove an agent from their agency",
            description = """
                    Removes an agent's membership from their associated agency.
                    Accessible to SUPER_ADMIN and AGENCY_OWNER roles.
                    The operation is subject to agency membership and authorization rules.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> deleteAgent(
            @PathVariable UUID agentId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        agentService.deleteAgentFromAgency(agentId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Agent removed successfully", null)
        );
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/me/properties")
    @Operation(
            summary = "Get agent's assigned properties",
            description = """
                    Retrieves a paginated list of properties assigned to the currently authenticated agent.
                    Supports property filtering and pagination.
                    Requires the AGENT role.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getOwnProperties(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @ModelAttribute PropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = agentService.getOwnAssignedProperties(currentUser, filter, pageable);

        ApiResponse<Page<PropertyResponse>> apiResponse =
                ApiResponse.success("Properties fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/me/inquiries")
    @Operation(
            summary = "Get agent's property inquiries",
            description = """
                    Retrieves a paginated list of inquiries associated with properties assigned to the
                    currently authenticated agent.
                    Supports inquiry filtering and pagination.
                    Requires the AGENT role.
                    """
    )
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getMyInquiries(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @ModelAttribute InquiryFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<InquiryResponse> response = agentService.getOwnInquiries(currentUser, filter, pageable);

        ApiResponse<Page<InquiryResponse>> apiResponse =
                ApiResponse.success("Inquiry list fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

}
