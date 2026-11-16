package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.PublicReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Reviews",
        description = """
                Endpoints for creating, retrieving, updating, and deleting property and agency reviews.
                Supports paginated review listings with filtering and authenticated review management.
                """
)
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/properties/{propertyId}/reviews")
    @Operation(
            summary = "Create a property review",
            description = """
                    Creates a review for the specified property.
                    Requires an authenticated user.
                    The review details are provided in the request body.
                    The authenticated user's information is used to associate the review with its author.
                    """
    )
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
    @Operation(
            summary = "Get property reviews",
            description = """
                    Retrieves a paginated list of reviews for the specified property.
                    Supports filtering through PublicReviewFilterRequest.
                    Results are sorted by creation date in descending order by default.
                    """
    )
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPropertyReviews(
            @PathVariable UUID propertyId,
            @ModelAttribute PublicReviewFilterRequest filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<ReviewResponse> response = reviewService.getPropertyReviews(propertyId, filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Property review list fetched successfully", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/agencies/{agencyId}/reviews")
    @Operation(
            summary = "Create an agency review",
            description = """
                    Creates a review for the specified agency.
                    Requires an authenticated user.
                    The review details are provided in the request body.
                    The authenticated user's information is used to associate the review with its author.
                    """
    )
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
    @Operation(
            summary = "Get agency reviews",
            description = """
                    Retrieves a paginated list of reviews for the specified agency.
                    Supports filtering through PublicReviewFilterRequest.
                    Results are sorted by creation date in descending order by default.
                    """
    )
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAgencyReviews(
            @PathVariable UUID agencyId,
            @ModelAttribute PublicReviewFilterRequest filter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<ReviewResponse> response = reviewService.getAgencyReviews(agencyId, filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Agency review list fetched successfully", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/reviews/{reviewId}")
    @Operation(
            summary = "Update own review",
            description = """
                    Updates an existing review identified by its ID.
                    Requires an authenticated user.
                    The updated review details are provided in the request body.
                    The authenticated user's information is used to verify review ownership.
                    """
    )
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

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/reviews/{reviewId}")
    @Operation(
            summary = "Delete own review",
            description = """
                    Deletes an existing review identified by its ID.
                    Requires an authenticated user.
                    The authenticated user's information is used to verify review ownership.
                    """
    )
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
