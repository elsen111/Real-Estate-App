package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.inquiry.request.CreateInquiryRequest;
import com.realestate.backend.dto.inquiry.request.UpdateInquiryStatusRequest;
import com.realestate.backend.dto.inquiry.response.InquiryResponse;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.inquiry.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping("/properties/{propertyId}/inquiries")
    @Operation(summary = "Create a new inquiry")
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

    @GetMapping("/inquiries/me")
    @Operation(summary = "Get client's inquiries")
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getMyInquiries(
            @RequestParam(required = false)InquiryStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<InquiryResponse> response = inquiryService.getClientInquiries(currentUser, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry list fetched successfully", response)
        );

    }

    @GetMapping("/agencies/inquiries")
    @Operation(summary = "Get agency's inquiries")
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getMyInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<InquiryResponse> response = inquiryService.getMyAgencyInquiries(currentUser, status, propertyId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry list fetched successfully", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/inquiries/{inquiryId}")
    @Operation(summary = "Get inquiry by id")
    public ResponseEntity<ApiResponse<InquiryResponse>> getInquiryById(
            @PathVariable UUID inquiryId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        InquiryResponse response = inquiryService.getInquiryById(currentUser, inquiryId);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry fetched successfully", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/inquiries/{inquiryId}/status")
    @Operation(summary = "Update inquiry status")
    public ResponseEntity<ApiResponse<InquiryResponse>> updateInquiryStatus(
            @PathVariable UUID inquiryId,
            @Valid @RequestBody UpdateInquiryStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        InquiryResponse response = inquiryService.updateStatus(currentUser, inquiryId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry status updated successfully", response)
        );

    }
}
