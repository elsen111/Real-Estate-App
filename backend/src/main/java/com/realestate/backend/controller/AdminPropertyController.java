package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.request.PropertyStatusRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.service.AdminPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/properties")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(
        name = "Admin - Property Management",
        description = """
                Administrative endpoints for managing platform properties, including property retrieval,
                filtering, pagination, and moderation through property status updates.
                """
)
public class AdminPropertyController {

    private final AdminPropertyService adminPropertyService;

    @GetMapping
    @Operation(
            summary = "Get all properties",
            description = """
                    Retrieves a paginated list of properties across the platform.
                    Supports filtering by property attributes and status, along with sorting and pagination.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getAllProperties(
            @ModelAttribute PropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = adminPropertyService.getAllProperties(filter, pageable);

        ApiResponse<Page<PropertyResponse>> apiResponse =
                ApiResponse.success("Properties fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }


    @PutMapping("/{propertyId}/status")
    @Operation(
            summary = "Change property status",
            description = """
                    Updates the status of a specific property using the provided status request.
                    Allows super administrators to moderate property listings by changing their current status.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> changePropertyStatus(
            @PathVariable @NotNull UUID propertyId,
            @Valid @RequestBody PropertyStatusRequest request
    ) {

        String message = adminPropertyService.changePropertyStatus(propertyId, request.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success(message, null)
        );

    }

}
