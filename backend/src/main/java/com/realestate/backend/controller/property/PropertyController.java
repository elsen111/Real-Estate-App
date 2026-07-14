package com.realestate.backend.controller.property;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.property.request.PropertyRequest;
import com.realestate.backend.dto.property.request.PropertyPublicFilterRequest;
import com.realestate.backend.dto.property.request.PropertyStatusRequest;
import com.realestate.backend.dto.property.response.PropertyDetailResponse;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.dto.property.response.PropertySearchSuggestionResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.property.PropertyService;
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
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

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

}
