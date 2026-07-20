package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.CreateReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/properties/{propertyId}/reviews")
    @Operation(summary = "Create a new review.")
    public ResponseEntity<ApiResponse<ReviewResponse>> createPropertyReview(
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        ReviewResponse response = reviewService.createPropertyReview(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Review created successfully", response)
        );

    }

}
