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
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.AgencyService;
import com.realestate.backend.service.AppointmentService;
import com.realestate.backend.service.InquiryService;
import com.realestate.backend.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Agency Management",
        description = """
                Endpoints for agency profile management, public agency discovery, subscription information,
                property listings, agency members, inquiries, and appointments.
                Includes both authenticated agency operations and publicly accessible agency information.
                """
)
public class AgencyController {

    private final AgencyService agencyService;
    private final InquiryService inquiryService;
    private final AppointmentService appointmentService;
    private final PropertyService propertyService;

    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    @GetMapping("/me")
    @Operation(
            summary = "Get current agency information",
            description = """
                    Retrieves the agency profile associated with the authenticated agency owner or agent,
                    including its details and current information.
                    """
    )
    public ResponseEntity<ApiResponse<AgencyResponse>> getCurrentAgency(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        AgencyResponse response = agencyService.getCurrentAgency(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Current agency information fetched successfully", response)
        );

    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(
            summary = "Update agency profile",
            description = """
                    Updates the authenticated agency owner's agency profile using the provided request data.
                    Only agency owners can modify their agency information.
                    """
    )
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
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(
            summary = "Upload agency logo",
            description = """
                    Uploads a new logo for the authenticated agency using multipart/form-data.
                    The uploaded image is associated with the agency profile, and its storage information is returned.
                    """
    )
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
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'AGENT', 'LANDLORD')")
    @Operation(
            summary = "Get current agency subscription",
            description = """
                    Retrieves the subscription information associated with the authenticated user's agency,
                    including the assigned subscription plan and subscription details.
                    """
    )
    public ResponseEntity<ApiResponse<AgencySubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        AgencySubscriptionResponse response = agencyService.getMySubscription(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription information fetched successfully", response)
        );

    }

    @GetMapping("/me/properties")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'AGENT')")
    @Operation(
            summary = "Get current agency properties",
            description = """
                    Retrieves a paginated list of properties belonging to the authenticated user's agency.
                    Supports filtering by property attributes and status, as well as sorting and pagination.
                    """
    )
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
    @Operation(
            summary = "Get all public agencies",
            description = """
                    Retrieves a paginated list of publicly available agencies on the platform.
                    Supports filtering by agency attributes and sorting and pagination.
                    """
    )
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
    @Operation(
            summary = "Get public agency information",
            description = """
                    Retrieves publicly accessible profile information for a specific agency using its unique identifier.
                    """
    )
    public ResponseEntity<ApiResponse<AgencyResponse>> getAgencyPublicInfo(
            @PathVariable UUID agencyId
    ) {

        AgencyResponse response = agencyService.getPublicAgencyInfo(agencyId);

        return ResponseEntity.ok(
                ApiResponse.success("Agency information fetched successfully", response)
        );

    }

    @GetMapping("/public/{agencyId}/properties")
    @Operation(
            summary = "Get agency public properties",
            description = """
                    Retrieves a paginated list of publicly available property listings belonging to a specific agency.
                    Supports filtering by property attributes, sorting, and pagination.
                    """
    )
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

    @PreAuthorize("hasAnyRole('AGENCY_OWNER','ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{agencyId}/agents")
    @Operation(
            summary = "Get agency members",
            description = """
                    Retrieves a paginated list of agents and members associated with a specific agency.
                    Supports filtering by member attributes and membership status, along with sorting and pagination.
                    """
    )
    public ResponseEntity<ApiResponse<Page<AgencyMemberResponse>>> getAgencyAgents(
            @PathVariable UUID agencyId,
            @ModelAttribute AgencyAgentFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<AgencyMemberResponse> response = agencyService.getAgencyAgents(agencyId, filter, pageable);

        ApiResponse<Page<AgencyMemberResponse>> apiResponse =
                ApiResponse.success("Agency's agents fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @DeleteMapping("/me/logo")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(
            summary = "Remove agency logo",
            description = """
                    Removes the currently configured logo from the authenticated agency's profile.
                    Only agency owners can perform this operation.
                    """
    )
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
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    @Operation(
            summary = "Get current agency inquiries",
            description = """
                    Retrieves a paginated list of inquiries associated with the authenticated user's agency.
                    Supports optional filtering by inquiry status and property identifier.
                    Results are sorted by creation date in descending order by default.
                    """
    )
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getMyAgencyInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<InquiryResponse> response = inquiryService.getMyAgencyInquiries(currentUser, status, propertyId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry list fetched successfully", response)
        );

    }

    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT', 'LANDLORD')")
    @GetMapping("/agencies/me/appointments")
    @Operation(
            summary = "Get current agency appointments",
            description = """
                    Retrieves a paginated list of appointments associated with the authenticated user's agency.
                    Supports optional filtering by appointment status and property identifier.
                    Results are sorted by creation date in descending order by default.
                    """
    )
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAgencyAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<AppointmentResponse> response = appointmentService.getMyAgencyAppointments(
                currentUser, status, propertyId,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success("Appointment list fetched successfully", response)
        );

    }

    @GetMapping("/public/top")
    @Operation(
            summary = "Get top-performing agencies",
            description = """
                    Retrieves a paginated list of top-performing agencies based on the platform's agency
                    performance ranking criteria. The default page size is 8.
                    """
    )
    public ResponseEntity<ApiResponse<Page<TopAgencyResponse>>> getTopAgencies(
            @PageableDefault(size = 8)
            Pageable pageable
    ) {

        Page<TopAgencyResponse> response = propertyService.getTopAgencies(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Trending cities fetched successfully", response)
        );

    }

}
