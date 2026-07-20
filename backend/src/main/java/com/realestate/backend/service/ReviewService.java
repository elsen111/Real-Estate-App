package com.realestate.backend.service;

import com.realestate.backend.dto.request.CreateReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.security.CustomUserDetails;

import java.util.UUID;

public interface ReviewService {

    ReviewResponse createPropertyReview(UUID propertyId, CreateReviewRequest request, CustomUserDetails currentUser);

}
