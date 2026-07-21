package com.realestate.backend.service;

import com.realestate.backend.dto.request.AdminReviewFilterRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReviewService {

    Page<ReviewResponse> getAllReviews(
            AdminReviewFilterRequest filter,
            Pageable pageable
    );

}
