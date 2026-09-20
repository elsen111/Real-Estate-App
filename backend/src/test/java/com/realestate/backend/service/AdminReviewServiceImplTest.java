package com.realestate.backend.service;

import com.realestate.backend.dto.request.ReviewStatusRequest;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.ReviewMapper;
import com.realestate.backend.repository.ReviewRepository;
import com.realestate.backend.service.impl.AdminReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private AdminReviewServiceImpl service;

    @Test
    void updateReviewStatus_throws_whenRevertingApprovedToPending() {
        UUID reviewId = UUID.randomUUID();

        ReviewEntity review = ReviewEntity.builder()
                .id(reviewId)
                .status(ReviewStatus.APPROVED)
                .build();

        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.PENDING);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThatThrownBy(() ->
                service.updateReviewStatus(reviewId, request)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot revert an already approved review");

        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateReviewStatus_throws_whenAlreadyRejected() {
        UUID reviewId = UUID.randomUUID();

        ReviewEntity review = ReviewEntity.builder()
                .id(reviewId)
                .status(ReviewStatus.REJECTED)
                .build();

        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.APPROVED);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        assertThatThrownBy(() ->
                service.updateReviewStatus(reviewId, request)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot revert an already rejected review");

        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateReviewStatus_throws_whenReviewNotFound() {
        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.APPROVED);

        assertThatThrownBy(() ->
                service.updateReviewStatus(reviewId, request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review not found with id");

        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateReviewStatus_succeeds_whenPendingToApproved_forNonPropertyReview() {
        UUID reviewId = UUID.randomUUID();

        ReviewEntity review = ReviewEntity.builder()
                .id(reviewId)
                .status(ReviewStatus.PENDING)
                .target(ReviewTargetType.AGENCY)
                .build();

        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.APPROVED);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        service.updateReviewStatus(reviewId, request);

        assertThat(review.getStatus())
                .isEqualTo(ReviewStatus.APPROVED);

        verify(reviewRepository)
                .saveAndFlush(review);

        verify(reviewRepository, never())
                .countByPropertyIdAndStatus(any(), any());

        verify(reviewRepository, never())
                .sumRatingByPropertyIdAndStatus(any(), any());
    }

    @Test
    void updateReviewStatus_updatesPropertyRating_whenPendingToApproved() {
        UUID reviewId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .reviewCount(0)
                .averageRating(BigDecimal.ZERO)
                .build();

        ReviewEntity review = ReviewEntity.builder()
                .id(reviewId)
                .status(ReviewStatus.PENDING)
                .target(ReviewTargetType.PROPERTY)
                .property(property)
                .build();

        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.APPROVED);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewRepository.countByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(3);

        when(reviewRepository.sumRatingByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(new BigDecimal("13"));

        service.updateReviewStatus(reviewId, request);

        assertThat(review.getStatus())
                .isEqualTo(ReviewStatus.APPROVED);

        assertThat(property.getReviewCount())
                .isEqualTo(3);

        assertThat(property.getAverageRating())
                .isEqualByComparingTo(new BigDecimal("4.33"));

        verify(reviewRepository)
                .saveAndFlush(review);

        verify(reviewRepository)
                .countByPropertyIdAndStatus(
                        propertyId,
                        ReviewStatus.APPROVED
                );

        verify(reviewRepository)
                .sumRatingByPropertyIdAndStatus(
                        propertyId,
                        ReviewStatus.APPROVED
                );
    }

    @Test
    void updateReviewStatus_setsZeroAverageRating_whenNoApprovedPropertyReviewsRemain() {
        UUID reviewId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .reviewCount(5)
                .averageRating(new BigDecimal("4.50"))
                .build();

        ReviewEntity review = ReviewEntity.builder()
                .id(reviewId)
                .status(ReviewStatus.APPROVED)
                .target(ReviewTargetType.PROPERTY)
                .property(property)
                .build();

        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.REJECTED);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewRepository.countByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(0);

        when(reviewRepository.sumRatingByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(BigDecimal.ZERO);

        service.updateReviewStatus(reviewId, request);

        assertThat(review.getStatus())
                .isEqualTo(ReviewStatus.REJECTED);

        assertThat(property.getReviewCount())
                .isZero();

        assertThat(property.getAverageRating())
                .isEqualByComparingTo(BigDecimal.ZERO);

        verify(reviewRepository)
                .saveAndFlush(review);

        verify(reviewRepository)
                .countByPropertyIdAndStatus(
                        propertyId,
                        ReviewStatus.APPROVED
                );

        verify(reviewRepository)
                .sumRatingByPropertyIdAndStatus(
                        propertyId,
                        ReviewStatus.APPROVED
                );
    }

    @Test
    void updateReviewStatus_recalculatesPropertyRating_whenApprovedToRejected() {
        UUID reviewId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .reviewCount(4)
                .averageRating(new BigDecimal("4.25"))
                .build();

        ReviewEntity review = ReviewEntity.builder()
                .id(reviewId)
                .status(ReviewStatus.APPROVED)
                .target(ReviewTargetType.PROPERTY)
                .property(property)
                .build();

        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.REJECTED);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewRepository.countByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(3);

        when(reviewRepository.sumRatingByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(new BigDecimal("12"));

        service.updateReviewStatus(reviewId, request);

        assertThat(review.getStatus())
                .isEqualTo(ReviewStatus.REJECTED);

        assertThat(property.getReviewCount())
                .isEqualTo(3);

        assertThat(property.getAverageRating())
                .isEqualByComparingTo(new BigDecimal("4.00"));

        verify(reviewRepository)
                .saveAndFlush(review);

        verify(reviewRepository)
                .countByPropertyIdAndStatus(
                        propertyId,
                        ReviewStatus.APPROVED
                );

        verify(reviewRepository)
                .sumRatingByPropertyIdAndStatus(
                        propertyId,
                        ReviewStatus.APPROVED
                );
    }
}