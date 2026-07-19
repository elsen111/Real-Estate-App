package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AppointmentService;
import com.realestate.backend.service.InquiryService;
import com.realestate.backend.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
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
public class PropertyController {

    private final PropertyService propertyService;
    private final InquiryService inquiryService;
    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Create a new property.")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
            ){

        PropertyResponse response = propertyService.createProperty(request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property created successfully", response)
        );

    }

    @GetMapping
    @Operation(summary = "Get all available public properties.")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllPublicProperties(
            @ModelAttribute PropertyPublicFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ){

        Page<PropertyResponse> response = propertyService.getAllPublicProperties(filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Properties fetched successfully", response)
        );

    }

    @GetMapping("/{propertyId}")
    @Operation(summary = "Get detailed information for the specific property.")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> getPropertyById(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        PropertyDetailResponse response = propertyService.getPropertyDetailsById(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property details fetched successfully", response)
        );

    }

    @PutMapping("/{propertyId}")
    @Operation(summary = "Update an existing property")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @Valid @RequestBody PropertyRequest request,
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        PropertyResponse response = propertyService.updateProperty(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property updated successfully", response)
        );

    }

    @PatchMapping("/{propertyId}")
    @Operation(summary = "Update the status of an existing property")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updatePropertyStatus(
            @Valid @RequestBody PropertyStatusRequest request,
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        propertyService.updateStatus(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property status successfully changed to " + request.getStatus(), null)
        );

    }

    @PatchMapping("/{propertyId}/featured")
    @Operation(summary = "Update the featured property of an existing property")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> toggleFeatured(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        PropertyResponse response = propertyService.toggleFeaturedProperty(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property updated successfully", response)
        );

    }

    @DeleteMapping("/{propertyId}")
    @Operation(summary = "Soft delete the existing property")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteProperty(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        propertyService.softDeleteProperty(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property deleted successfully", null)
        );

    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured properties.")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getFeaturedProperties(
            @ModelAttribute PropertyPublicFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ){

        Page<PropertyResponse> response = propertyService.getFeaturedProperties(filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Featured properties fetched successfully", response)
        );

    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent properties.")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getRecentProperties(
            @ModelAttribute PropertyPublicFilterRequest filter,
            @RequestParam(defaultValue = "8") int size
    ){

        Page<PropertyResponse> response = propertyService.getRecentProperties(filter, size);

        return ResponseEntity.ok(
                ApiResponse.success("Recent properties fetched successfully", response)
        );

    }

    @GetMapping("/{propertyId}/similar")
    @Operation(summary = "Get similar properties.")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getSimilarProperties(
            @PathVariable  UUID propertyId,
            @PageableDefault(sort = "featured")
            Pageable pageable
    ){

        Page<PropertyResponse> response = propertyService.getSimilarProperties(propertyId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Similar properties fetched successfully", response)
        );

    }

    @GetMapping("/search/suggestions")
    @Operation(summary = "Get search suggestion according to the typed keyword.")
    public ResponseEntity<ApiResponse<PropertySearchSuggestionResponse>> getSuggestedProperties(
            @Valid @RequestParam String keyword
    ){

        PropertySearchSuggestionResponse response = propertyService.getSearchSuggestions(keyword);

        return ResponseEntity.ok(
                ApiResponse.success("Suggested properties fetched successfully", response)
        );

    }

    @GetMapping("/map")
    @Operation(summary = "Get properties with map details.")
    public ResponseEntity<ApiResponse<Page<PropertyMapResponse>>> getMapProperties(
            @ModelAttribute PropertyMapFilterRequest request,
            @PageableDefault(size = 100)
            Pageable pageable
    ){

        Page<PropertyMapResponse> response = propertyService.getMapProperties(request, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Properties map details fetched successfully", response)
        );

    }

    @PostMapping(
            value = "/{propertyId}/media",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload property media files.")
    public ResponseEntity<ApiResponse<List<PropertyMediaResponse>>> uploadPropertyMedia(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestPart("files") List<MultipartFile> files
    ){

        List<PropertyMediaResponse> response = propertyService.uploadMedia(propertyId, files, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property media files uploaded successfully", response)
        );

    }

    @GetMapping("/{propertyId}/images")
    @Operation(summary = "Get property images.")
    public ResponseEntity<ApiResponse<List<PropertyMediaResponse>>> getPropertyMedia(
        @PathVariable UUID propertyId
    ){

        List<PropertyMediaResponse> response = propertyService.getPropertyMedia(propertyId);

        return ResponseEntity.ok(
                ApiResponse.success("Property media files fetched successfully", response)
        );

    }

    @PatchMapping("/{propertyId}/media/{mediaId}")
    @Operation(summary = "Set the primary image for property")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    public ResponseEntity<ApiResponse<List<SetPropertyMediaResponse>>> setPrimaryImage(
            @PathVariable UUID propertyId,
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        List<SetPropertyMediaResponse> response = propertyService.setPrimaryImage(propertyId, mediaId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Media has been set as primary", response)
        );

    }

    @DeleteMapping("/media/{mediaId}")
    @Operation(summary = "Delete the media image.")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        propertyService.removePropertyMediaFile(currentUser, mediaId);

        return ResponseEntity.ok(
                ApiResponse.success("Media image deleted successfully", null)
        );

    }

    @PostMapping("/{propertyId}/inquiries")
    @Operation(summary = "Create a new inquiry for the specific property")
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

    @PostMapping("/{propertyId}/appointments")
    @Operation(summary = "Create a new appointment for the speicific property.")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment (
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        AppointmentResponse response = appointmentService.createAppointment(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment created successfully", response)
        );

    }

}
