package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.PublicReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.ReviewMapper;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.ReviewRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.repository.specification.ReviewSpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Slf4j
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

        if (!isClient) {
            throw new ForbiddenException("Only client users are allowed to create reviews");
        }

        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found with id: " + propertyId);
        }

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        if (reviewRepository.existsByReviewerIdAndPropertyId(user.getId(), propertyId)) {
            throw new ConflictException("You already have a review for this property.");
        }

        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property not found with id: " + propertyId
                        ));

        ReviewEntity createdReview = reviewMapper.toEntity(
                request,
                property,
                user,
                null
        );

        createdReview.setTarget(ReviewTargetType.PROPERTY);

        ReviewEntity savedReview = reviewRepository.saveAndFlush(createdReview);

        if (savedReview.getStatus() == ReviewStatus.APPROVED) {
            ReviewStatus status = ReviewStatus.APPROVED;

            int currentReviewCount = reviewRepository.countByPropertyIdAndStatus(propertyId, status);
            savedReview.getProperty().setReviewCount(currentReviewCount);

            BigDecimal totalPoints = reviewRepository.sumRatingByPropertyIdAndStatus(propertyId, status);

            if (currentReviewCount > 0) {
                BigDecimal currentAvgRating = totalPoints.divide(
                        BigDecimal.valueOf(currentReviewCount),
                        2,
                        RoundingMode.HALF_UP
                );
                savedReview.getProperty().setAverageRating(currentAvgRating);
            } else {
                savedReview.getProperty().setAverageRating(BigDecimal.ZERO);
            }

        }


        log.atInfo()
                .setMessage("Review created by user for property")
                .addKeyValue("userId", user.getId())
                .addKeyValue("reviewId", savedReview.getId())
                .addKeyValue("propertyId", propertyId)
                .log();

        return reviewMapper.toResponse(savedReview);

    }

    @Transactional(readOnly = true)
    @Override
    public Page<ReviewResponse> getPropertyReviews(
            UUID propertyId, PublicReviewFilterRequest filterRequest, Pageable pageable) {

        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found with id: " + propertyId);
        }

        Specification<ReviewEntity> specification = ReviewSpecification.withPublicFilter(
                null, propertyId, filterRequest);

        return reviewRepository.findAll(specification, pageable).map(reviewMapper::toResponse);

    }

    @Override
    @Transactional
    public ReviewResponse createAgencyReview(UUID agencyId, ReviewRequest request, CustomUserDetails currentUser) {

        boolean isClient = currentUser.getAuthorities().stream()
                .allMatch(auth -> Objects.equals(auth.getAuthority(), "ROLE_CLIENT"));

        if (!isClient) {
            throw new ForbiddenException("Only client users are allowed to create reviews");
        }

        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id: " + agencyId)
                );

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        if (reviewRepository.existsByReviewerIdAndAgencyId(user.getId(), agencyId)) {
            throw new ConflictException("You already have a review for this agency.");
        }

        ReviewEntity createdReview = reviewMapper.toEntity(request, null, user, agency);
        createdReview.setTarget(ReviewTargetType.AGENCY);

        ReviewEntity savedReview = reviewRepository.saveAndFlush(createdReview);

        log.atInfo()
                .setMessage("Review created by user for agency")
                .addKeyValue("userId", user.getId())
                .addKeyValue("reviewId", savedReview.getId())
                .addKeyValue("agencyId", agencyId)
                .log();

        return reviewMapper.toResponse(savedReview);

    }

    @Transactional(readOnly = true)
    @Override
    public Page<ReviewResponse> getAgencyReviews(
            UUID agencyId, PublicReviewFilterRequest filterRequest, Pageable pageable) {

        if (!agencyRepository.existsById(agencyId)) {
            throw new ResourceNotFoundException("Agency not found with id: " + agencyId);
        }

        Specification<ReviewEntity> specification = ReviewSpecification.withPublicFilter(agencyId, null, filterRequest);

        return reviewRepository.findAll(specification, pageable).map(reviewMapper::toResponse);

    }

    @Override
    @Transactional
    public ReviewResponse updateOwnReview(UUID reviewId, ReviewRequest request, CustomUserDetails currentUser) {

        if (!reviewRepository.existsByIdAndReviewerId(reviewId, currentUser.getId())) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Review not found with id " + reviewId)
                );

        reviewMapper.toEntity(request, user, review);

        ReviewEntity savedReview = reviewRepository.saveAndFlush(review);

        if (savedReview.getTarget() == ReviewTargetType.PROPERTY && savedReview.getProperty() != null) {
            UUID propertyId = savedReview.getProperty().getId();
            ReviewStatus status = ReviewStatus.APPROVED;

            if (savedReview.getStatus() == status) {
                int currentReviewCount = reviewRepository.countByPropertyIdAndStatus(propertyId, status);
                BigDecimal totalPoints = reviewRepository.sumRatingByPropertyIdAndStatus(propertyId, status);

                savedReview.getProperty().setReviewCount(currentReviewCount);

                if (currentReviewCount > 0) {
                    BigDecimal currentAvgRating = totalPoints.divide(
                            BigDecimal.valueOf(currentReviewCount),
                            2,
                            RoundingMode.HALF_UP
                    );
                    savedReview.getProperty().setAverageRating(currentAvgRating);
                } else {
                    savedReview.getProperty().setAverageRating(BigDecimal.ZERO);
                }
            }
        }

        log.atInfo()
                .setMessage("Review updated by user")
                .addKeyValue("userId", currentUser.getId())
                .addKeyValue("reviewId", reviewId)
                .log();

        return reviewMapper.toResponse(savedReview);

    }

    @Override
    @Transactional
    public void deleteOwnReview(UUID reviewId, CustomUserDetails currentUser) {

        ReviewEntity review = reviewRepository.findByIdAndReviewerId(reviewId, currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Review not found with id: " + reviewId)
                );

        ReviewTargetType target = review.getTarget();
        ReviewStatus status = review.getStatus();
        var property = review.getProperty();

        reviewRepository.delete(review);
        reviewRepository.flush();

        if (target == ReviewTargetType.PROPERTY && property != null && status == ReviewStatus.APPROVED) {
            UUID propertyId = property.getId();
            ReviewStatus approvedStatus = ReviewStatus.APPROVED;

            int currentReviewCount = reviewRepository.countByPropertyIdAndStatus(propertyId, approvedStatus);
            BigDecimal totalPoints = reviewRepository.sumRatingByPropertyIdAndStatus(propertyId, approvedStatus);

            property.setReviewCount(currentReviewCount);

            if (currentReviewCount > 0) {
                BigDecimal currentAvgRating = totalPoints.divide(
                        BigDecimal.valueOf(currentReviewCount),
                        2,
                        RoundingMode.HALF_UP
                );
                property.setAverageRating(currentAvgRating);
            } else {
                property.setAverageRating(BigDecimal.ZERO);
            }
        }

        log.atInfo()
                .setMessage("Review deleted by user")
                .addKeyValue("userId", currentUser.getId())
                .addKeyValue("reviewId", reviewId)
                .log();

    }

}
