package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.*;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.AppointmentService;
import com.realestate.backend.service.InquiryService;
import com.realestate.backend.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@Tag(
        name = "Properties",
        description = """
                APIs for property management and discovery, including creating and updating
                property listings, browsing and searching public properties, map-based property
                discovery, featured and popular listings, property media management,
                inquiry and appointment creation, and agent assignment.
                """
)
public class PropertyController {

    private final PropertyService propertyService;
    private final InquiryService inquiryService;
    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @Operation(
            summary = "Create a new property listing",
            description = """
                    Creates a new property listing using the provided property details.
                    The property is associated with the authenticated agency owner's account
                    and agency.
                    Requires the AGENCY_OWNER role.
                    """
    )
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        PropertyResponse response = propertyService.createProperty(request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property created successfully", response)
        );

    }

    @GetMapping
    @Operation(
            summary = "Get all public property listings",
            description = """
                    Retrieves a paginated list of publicly available property listings.
                    Supports filtering by property attributes and other search criteria.
                    Supports pagination and sorting.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllPublicProperties(
            @ModelAttribute PropertyPublicFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = propertyService.getAllPublicProperties(filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Properties fetched successfully", response)
        );

    }

    @GetMapping("/{propertyId}")
    @Operation(
            summary = "Get property details by ID",
            description = """
                    Retrieves detailed information about a specific property using its unique identifier.
                    The returned information may depend on the property's visibility and
                    the authenticated user's permissions.
                    """
    )
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> getPropertyById(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        PropertyDetailResponse response = propertyService.getPropertyDetailsById(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property details fetched successfully", response)
        );

    }

    @PutMapping("/{propertyId}")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    @Operation(
            summary = "Update an existing property listing",
            description = """
                    Updates the details of an existing property listing using the provided request data.
                    Requires the AGENCY_OWNER or AGENT role.
                    The authenticated user must have permission to manage the specified property.
                    """
    )
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @Valid @RequestBody PropertyRequest request,
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        PropertyResponse response = propertyService.updateProperty(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property updated successfully", response)
        );

    }

    @PatchMapping("/{propertyId}")
    @Operation(
            summary = "Update property listing status",
            description = """
                    Changes the status of an existing property listing using the requested status.
                    Supports authorized agency personnel and administrative users.
                    Requires the AGENCY_OWNER, AGENT, SUPER_ADMIN, or ADMIN role.
                    """
    )
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT', 'SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updatePropertyStatus(
            @Valid @RequestBody PropertyStatusRequest request,
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        propertyService.updateStatus(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property status successfully changed to " + request.getStatus(), null)
        );

    }

    @PatchMapping("/{propertyId}/featured")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Toggle a property's featured status",
            description = """
                    Toggles the featured status of an existing property listing.
                    The operation is subject to the user's authorization and applicable
                    agency subscription limits.
                    Requires the AGENCY_OWNER, AGENT, SUPER_ADMIN, or ADMIN role.
                    """
    )
    public ResponseEntity<ApiResponse<PropertyResponse>> toggleFeatured(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        PropertyResponse response = propertyService.toggleFeaturedProperty(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property updated successfully", response)
        );

    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(
            summary = "Soft delete a property listing",
            description = """
                    Soft deletes an existing property listing without permanently removing
                    its database record.
                    Requires the AGENCY_OWNER, SUPER_ADMIN, or ADMIN role.
                    The authenticated user must be authorized to manage the specified property.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> softDeleteProperty(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        propertyService.softDeleteProperty(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property deleted successfully", null)
        );

    }

    @GetMapping("/featured")
    @Operation(
            summary = "Get featured property listings",
            description = """
                    Retrieves a paginated list of currently featured public property listings.
                    Supports filtering by property attributes and other search criteria.
                    Supports pagination and sorting.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getFeaturedProperties(
            @ModelAttribute PropertyPublicFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = propertyService.getFeaturedProperties(filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Featured properties fetched successfully", response)
        );

    }

    @GetMapping("/recent")
    @Operation(
            summary = "Get recently added properties",
            description = """
                    Retrieves recently created public property listings.
                    Supports filtering by property attributes and allows the client to specify
                    the maximum number of results through the size query parameter.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getRecentProperties(
            @ModelAttribute PropertyPublicFilterRequest filter,
            @RequestParam(defaultValue = "8") int size
    ) {

        Page<PropertyResponse> response = propertyService.getRecentProperties(filter, size);

        return ResponseEntity.ok(
                ApiResponse.success("Recent properties fetched successfully", response)
        );

    }

    @GetMapping("/popular")
    @Operation(
            summary = "Get the most viewed properties",
            description = """
                    Retrieves a paginated list of popular property listings based on their
                    view counts.
                    Supports pagination and sorting.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getPopularProperties(
            @PageableDefault(size = 3) Pageable pageable
    ) {

        Page<PropertyResponse> response = propertyService.getPopularProperties(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Popular properties fetched successfully", response)
        );

    }


    @GetMapping("/{propertyId}/similar")
    @Operation(
            summary = "Get properties similar to a specified property",
            description = """
                    Retrieves a paginated list of property listings similar to the specified property.
                    Uses the reference property's attributes to identify relevant listings.
                    Supports pagination and sorting.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getSimilarProperties(
            @PathVariable UUID propertyId,
            @PageableDefault(sort = "featured")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = propertyService.getSimilarProperties(propertyId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Similar properties fetched successfully", response)
        );

    }

    @GetMapping("/search/suggestions")
    @Operation(
            summary = "Get property search suggestions",
            description = """
                    Retrieves property search suggestions based on the supplied keyword.
                    Intended to provide relevant suggestions while users enter search terms.
                    """
    )
    public ResponseEntity<ApiResponse<PropertySearchSuggestionResponse>> getSuggestedProperties(
            @Valid @RequestParam String keyword
    ) {

        PropertySearchSuggestionResponse response = propertyService.getSearchSuggestions(keyword);

        return ResponseEntity.ok(
                ApiResponse.success("Suggested properties fetched successfully", response)
        );

    }

    @GetMapping("/map")
    @Operation(
            summary = "Get properties for map-based discovery",
            description = """
                    Retrieves a paginated list of properties with location information
                    required for displaying property markers on a map.
                    Supports filtering through map-specific search criteria.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyMapResponse>>> getMapProperties(
            @ModelAttribute PropertyMapFilterRequest request,
            @PageableDefault(size = 100)
            Pageable pageable
    ) {

        Page<PropertyMapResponse> response = propertyService.getMapProperties(request, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Properties map details fetched successfully", response)
        );

    }

    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'AGENT')")
    @PostMapping(
            value = "/{propertyId}/media",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Upload media files for a property",
            description = """
                    Uploads multiple image or media files associated with the specified property.
                    Files are submitted using multipart/form-data.
                    Requires the AGENCY_OWNER or AGENT role.
                    The authenticated user must have permission to manage the property.
                    """
    )
    public ResponseEntity<ApiResponse<List<PropertyMediaResponse>>> uploadPropertyMedia(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestPart("files") List<MultipartFile> files
    ) {

        List<PropertyMediaResponse> response = propertyService.uploadMedia(propertyId, files, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property media files uploaded successfully", response)
        );

    }

    @GetMapping("/{propertyId}/images")
    @Operation(
            summary = "Get property media files",
            description = """
                    Retrieves the media files associated with the specified property,
                    including their metadata and primary-image information.
                    """
    )
    public ResponseEntity<ApiResponse<List<PropertyMediaResponse>>> getPropertyMedia(
            @PathVariable UUID propertyId
    ) {

        List<PropertyMediaResponse> response = propertyService.getPropertyMedia(propertyId);

        return ResponseEntity.ok(
                ApiResponse.success("Property media files fetched successfully", response)
        );

    }

    @PatchMapping("/{propertyId}/media/{mediaId}")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    @Operation(
            summary = "Set the primary image of a property",
            description = """
                    Sets the specified property media file as the primary image of the property.
                    Updates the primary-image designation among the property's associated media files.
                    Requires the AGENCY_OWNER or AGENT role.
                    """
    )
    public ResponseEntity<ApiResponse<List<SetPropertyMediaResponse>>> setPrimaryImage(
            @PathVariable UUID propertyId,
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        List<SetPropertyMediaResponse> response = propertyService.setPrimaryImage(propertyId, mediaId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Media has been set as primary", response)
        );

    }

    @DeleteMapping("/media/{mediaId}")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    @Operation(
            summary = "Delete a property media file",
            description = """
                    Removes the specified media file from its associated property.
                    Requires the AGENCY_OWNER or AGENT role.
                    The authenticated user must have permission to manage the associated property.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        propertyService.removePropertyMediaFile(currentUser, mediaId);

        return ResponseEntity.ok(
                ApiResponse.success("Media image deleted successfully", null)
        );

    }

    //    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT', 'LANDLORD')")
    @PostMapping("/{propertyId}/inquiries")
    @Operation(
            summary = "Create an inquiry for a property",
            description = """
                    Creates a new inquiry associated with the specified property using
                    the provided inquiry details.
                    The inquiry is processed according to the property's availability
                    and applicable business rules.
                    """
    )
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateInquiryRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        InquiryResponse response = inquiryService.createInquiry(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry created successfully", response)
        );

    }

    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT', 'LANDLORD')")
    @PostMapping("/{propertyId}/appointments")
    @Operation(
            summary = "Create an appointment for a property",
            description = """
                    Creates a new appointment request for the specified property using
                    the provided appointment details.
                    Requires the AGENCY_OWNER, AGENT, or LANDLORD role.
                    The appointment is subject to the applicable scheduling and authorization rules.
                    """
    )
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        AppointmentResponse response = appointmentService.createAppointment(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment created successfully", response)
        );

    }


    @PreAuthorize("hasRole('AGENCY_OWNER')")
    @PutMapping("/{propertyId}/agent")
    @Operation(
            summary = "Assign an agent to a property",
            description = """
                    Assigns an existing agency agent to the specified property.
                    The assignment is performed within the authenticated agency owner's agency.
                    Requires the AGENCY_OWNER role.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> assignAgentToProperty(
            @PathVariable UUID propertyId,
            @Valid @RequestBody AssignAgentToPropertyRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        propertyService.assignAgentToProperty(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Agent assigned to the property successfully", null)
        );

    }

    @GetMapping("/cities/popular")
    @Operation(
            summary = "Get popular cities by property listings",
            description = """
                    Retrieves a paginated list of popular cities based on property listing
                    statistics.
                    Intended to support location-based property discovery.
                    Supports pagination and sorting.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PopularCityResponse>>> getTrendingCities(
            @PageableDefault(size = 8)
            Pageable pageable
    ) {

        Page<PopularCityResponse> response = propertyService.getTrendingCities(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Trending cities fetched successfully", response)
        );

    }

}
