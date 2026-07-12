package com.realestate.backend.controller.property;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.property.request.PropertyRequest;
import com.realestate.backend.dto.property.request.PropertyPublicFilterRequest;
import com.realestate.backend.dto.property.request.PropertyStatusRequest;
import com.realestate.backend.dto.property.response.PropertyDetailResponse;
import com.realestate.backend.dto.property.response.PropertyResponse;
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
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
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

}
