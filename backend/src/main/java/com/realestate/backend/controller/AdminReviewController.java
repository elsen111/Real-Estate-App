package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewStatusRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.service.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    @Operation(summary = "Get all reviews.")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAllReviews(
            @ModelAttribute AdminReviewFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<ReviewResponse> response = adminReviewService.getAllReviews(filter, pageable);

        ApiResponse<Page<ReviewResponse>> apiResponse =
                ApiResponse.success("Reviews fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{reviewId}/status")
    @Operation(summary = "Update the review status.")
    public ResponseEntity<ApiResponse<Void>> updateReviewStatus(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewStatusRequest request
    ) {

        adminReviewService.updateReviewStatus(reviewId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Review status changed successfully", null)
        );

    }

}
