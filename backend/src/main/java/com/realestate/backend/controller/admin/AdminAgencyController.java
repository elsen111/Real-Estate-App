package com.realestate.backend.controller.admin;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.agency.request.AgencyStatusRequest;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.user.request.DeleteAccountRequest;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.admin.agency.AdminAgencyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/agencies")
@RequiredArgsConstructor
public class AdminAgencyController {

    private final AdminAgencyService adminAgencyService;

    @GetMapping
    @Operation(summary = "Get all agencies")
    public ResponseEntity<ApiResponse<Page<AdminAgencyResponse>>> getAllAgencies(
            @ModelAttribute AdminAgencyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
            ) {

        Page<AdminAgencyResponse> response = adminAgencyService.getAllAgencies(filter, pageable);

        ApiResponse<Page<AdminAgencyResponse>> apiResponse =
                ApiResponse.success("Agencies fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @GetMapping("/{agencyId}")
    @Operation(summary = "Get agency by id")
    public ResponseEntity<ApiResponse<AdminAgencyResponse>> getAgencyById(
            @PathVariable UUID agencyId
    ) {

        AdminAgencyResponse response = adminAgencyService.getAgencyById(agencyId);

        return ResponseEntity.ok(
                ApiResponse.success("Agency fetched successfully", response)
        );

    }

    @PutMapping("/{agencyId}/status")
    @Operation(summary = "Change agency status")
    public ResponseEntity<ApiResponse<Void>> changeAgencyStatus(
            @PathVariable @NotNull UUID agencyId,
            @Valid @RequestBody AgencyStatusRequest request
    ) {

        String message = adminAgencyService.changeAgencyStatus(agencyId, request.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success(message, null)
        );

    }


    @Transactional
    @DeleteMapping("/{agencyId}")
    @Operation(summary = "Delete a specific agency")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable @NotNull UUID agencyId
    ) {

        adminAgencyService.softDeleteAgency(agencyId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Agency successfully disabled", null
                )
        );
    }

}
