package com.realestate.backend.service;

import com.realestate.backend.dto.request.CreateReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {

    ReviewResponse createPropertyReview(UUID propertyId, CreateReviewRequest request, CustomUserDetails currentUser);

    Page<ReviewResponse> getPropertyReviews(UUID propertyId, Pageable pageable);

    ReviewResponse createAgencyReview(UUID agencyId, CreateReviewRequest request, CustomUserDetails currentUser);

    Page<ReviewResponse> getAgencyReviews(UUID agencyId, Pageable pageable);

}
