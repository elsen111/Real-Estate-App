package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AdminReviewFilterRequest;
import com.realestate.backend.dto.request.AgencyFilterRequest;
import com.realestate.backend.dto.response.AgencyResponse;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.mapper.ReviewMapper;
import com.realestate.backend.repository.ReviewRepository;
import com.realestate.backend.repository.specification.AgencySpecification;
import com.realestate.backend.repository.specification.ReviewSpecification;
import com.realestate.backend.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public Page<ReviewResponse> getAllReviews(AdminReviewFilterRequest filter, Pageable pageable) {

        Specification<ReviewEntity> specification = ReviewSpecification
                .withAdminFilter(filter);

        return reviewRepository.findAll(specification, pageable)
                .map(reviewMapper::toResponse);
    }

}
