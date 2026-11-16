package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewStatusRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.service.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Admin - Review Management",
        description = """
                Administrative endpoints for managing platform reviews, including retrieving,
                filtering, and moderating reviews through status updates.
                """
)
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    @Operation(
            summary = "Get all reviews",
            description = """
                    Retrieves a paginated list of platform reviews.
                    Supports filtering by review attributes and status, as well as sorting and pagination.
                    """
    )
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
    @Operation(
            summary = "Update review status",
            description = """
                    Updates the moderation status of a specific review using the provided request.
                    Allows administrators to manage review visibility and moderation state.
                    """
    )
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
