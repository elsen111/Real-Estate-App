package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.ReviewMapper;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.ReviewRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    private final PropertyRepository propertyRepository;

    private final AgencyRepository agencyRepository;

    private final UserRepository userRepository;


    @Override
    @Transactional
    public ReviewResponse createPropertyReview(UUID propertyId, ReviewRequest request, CustomUserDetails currentUser) {

        boolean isClient = currentUser.getAuthorities().stream()
                .allMatch(auth -> Objects.equals(auth.getAuthority(), "ROLE_CLIENT"));

        if(!isClient) {
            throw new ForbiddenException("Only client users are allowed to create reviews");
        }

        if(!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property with id " + propertyId + " not found");
        }

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User with id " + currentUser.getId() + " not found")
                );

        if(reviewRepository.existsByReviewerIdAndPropertyId(user.getId(), propertyId)) {
            throw new BusinessException("You already have a review for this property.");
        }

        ReviewEntity createdReview = reviewMapper.toEntity(request, propertyId, user, null);
        createdReview.setTarget(ReviewTargetType.PROPERTY);

        ReviewEntity savedReview = reviewRepository.saveAndFlush(createdReview);

        return  reviewMapper.toResponse(savedReview);

    }

    @Override
    public Page<ReviewResponse> getPropertyReviews(UUID propertyId, Pageable pageable) {

        if(!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property with id " + propertyId + " not found");
        }

        return reviewRepository.findAllByPropertyIdAndStatusIs(propertyId, pageable, ReviewStatus.APPROVED).map(reviewMapper::toResponse);

    }

    @Override
    @Transactional
    public ReviewResponse createAgencyReview(UUID agencyId, ReviewRequest request, CustomUserDetails currentUser) {

        boolean isClient = currentUser.getAuthorities().stream()
                .allMatch(auth -> Objects.equals(auth.getAuthority(), "ROLE_CLIENT"));

        if(!isClient) {
            throw new ForbiddenException("Only client users are allowed to create reviews");
        }

        AgencyEntity agency = agencyRepository.findById(agencyId).orElseThrow(
                () -> new ResourceNotFoundException("Agency with id " + agencyId + " not found")
        );

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User with id " + currentUser.getId() + " not found")
                );

        if(reviewRepository.existsByReviewerIdAndAgencyId(user.getId(), agencyId)) {
            throw new BusinessException("You already have a review for this agency.");
        }

        ReviewEntity createdReview = reviewMapper.toEntity(request, null, user, agency);
        createdReview.setTarget(ReviewTargetType.AGENCY);

        ReviewEntity savedReview = reviewRepository.saveAndFlush(createdReview);

        return  reviewMapper.toResponse(savedReview);

    }

    @Override
    public Page<ReviewResponse> getAgencyReviews(UUID agencyId, Pageable pageable) {

        if(!agencyRepository.existsById(agencyId)) {
            throw new ResourceNotFoundException("Agency with id " + agencyId + " not found");
        }

        return reviewRepository.findAllByAgencyIdAndStatusIs(agencyId, pageable, ReviewStatus.APPROVED).map(reviewMapper::toResponse);

    }

    @Override
    @Transactional
    public ReviewResponse updateOwnReview(UUID reviewId, ReviewRequest request, CustomUserDetails currentUser) {

        if(!reviewRepository.existsByIdAndReviewerId(reviewId, currentUser.getId())) {
            throw new ResourceNotFoundException("Review with id " + reviewId + " not found");
        }

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User with id " + currentUser.getId() + " not found")
                );

        ReviewEntity review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new ResourceNotFoundException("Review with id " + reviewId + " not found")
        );

        reviewMapper.toEntity(request, user, review);

        ReviewEntity  savedReview = reviewRepository.saveAndFlush(review);

        return  reviewMapper.toResponse(savedReview);

    }

    @Override
    @Transactional
    public void deleteOwnReview(UUID reviewId, CustomUserDetails currentUser) {

        ReviewEntity review = reviewRepository.findByIdAndReviewerId(reviewId, currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Review with id " + reviewId + " not found")
                );

        reviewRepository.delete(review);

    }


}
