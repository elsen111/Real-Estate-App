package com.realestate.backend.controller.admin;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.admin.agency.request.AgencyStatusRequest;
import com.realestate.backend.dto.admin.property.request.AdminPropertyFilterRequest;
import com.realestate.backend.dto.admin.property.request.PropertyStatusRequest;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import com.realestate.backend.service.admin.property.AdminPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/properties")
@RequiredArgsConstructor
public class AdminPropertyController {

    private final AdminPropertyService  adminPropertyService;

    @GetMapping
    @Operation(summary = "Get all properties")
    public ResponseEntity<ApiResponse<Page<AdminPropertyResponse>>> getAllProperties(
            @ModelAttribute AdminPropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
            ) {

        Page<AdminPropertyResponse> response = adminPropertyService.getAllProperties(filter, pageable);

        ApiResponse<Page<AdminPropertyResponse>> apiResponse =
                ApiResponse.success("Properties fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }


    @PutMapping("/{propertyId}/status")
    @Operation(summary = "Change the status of a property")
    public ResponseEntity<ApiResponse<Void>> changeAgencyStatus(
            @PathVariable @NotNull UUID propertyId,
            @Valid @RequestBody PropertyStatusRequest request
    ) {

        String message = adminPropertyService.changePropertyStatus(propertyId, request.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success(message, null)
        );

    }

}
