package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.ReviewService;
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
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/properties/{propertyId}/reviews")
    @Operation(summary = "Create a new review for property.")
    public ResponseEntity<ApiResponse<ReviewResponse>> createPropertyReview(
            @PathVariable UUID propertyId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        ReviewResponse response = reviewService.createPropertyReview(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Review created successfully", response)
        );

    }

    @GetMapping("/properties/{propertyId}/reviews")
    @Operation(summary = "Get property reviews.")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPropertyReviews(
            @PathVariable UUID propertyId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<ReviewResponse> response = reviewService.getPropertyReviews(propertyId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Property review list fetched successfully", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/agencies/{agencyId}/reviews")
    @Operation(summary = "Create a new review for agency.")
    public ResponseEntity<ApiResponse<ReviewResponse>> createAgencyReview(
            @PathVariable UUID agencyId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        ReviewResponse response = reviewService.createAgencyReview(agencyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Review created successfully", response)
        );

    }

    @GetMapping("/agencies/{agencyId}/reviews")
    @Operation(summary = "Get agency reviews.")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAgencyReviews(
            @PathVariable UUID agencyId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<ReviewResponse> response = reviewService.getAgencyReviews(agencyId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Agency review list fetched successfully", response)
        );

    }

    @PutMapping("/reviews/{reviewId}")
    @Operation(summary = "Update own review.")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateOwnReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        ReviewResponse response = reviewService.updateOwnReview(reviewId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Review updated successfully", response)
        );

    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "Delete own review.")
    public ResponseEntity<ApiResponse<Void>> deleteOwnReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        reviewService.deleteOwnReview(reviewId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Review deleted successfully", null)
        );

    }

}
