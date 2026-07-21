package com.realestate.backend.service;

import com.realestate.backend.dto.request.AdminReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewStatusRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminReviewService {

    Page<ReviewResponse> getAllReviews(
            AdminReviewFilterRequest filter,
            Pageable pageable
    );

    void updateReviewStatus(UUID reviewId, ReviewStatusRequest request);

}
