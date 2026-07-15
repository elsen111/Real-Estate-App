package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.inquiry.request.CreateInquiryRequest;
import com.realestate.backend.dto.inquiry.response.InquiryClientResponse;
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
    public ResponseEntity<ApiResponse<InquiryClientResponse>> createInquiry(
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateInquiryRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        InquiryClientResponse response = inquiryService.createInquiry(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry created successfully", response)
        );

    }

    @GetMapping("/inquiries/me")
    @Operation(summary = "Get client's inquiries")
    public ResponseEntity<ApiResponse<Page<InquiryClientResponse>>> getMyInquiries(
            @RequestParam(required = false)InquiryStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<InquiryClientResponse> response = inquiryService.getClientInquiries(currentUser, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry list fetched successfully", response)
        );

    }
}
