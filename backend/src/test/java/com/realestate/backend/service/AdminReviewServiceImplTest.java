package com.realestate.backend.service;

import com.realestate.backend.dto.request.ReviewStatusRequest;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.ReviewRepository;
import com.realestate.backend.service.impl.AdminReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;

    @InjectMocks private AdminReviewServiceImpl service;

    @Test
    void updateReviewStatus_throws_whenRevertingApprovedToPending() {
        UUID reviewId = UUID.randomUUID();
        ReviewEntity review = ReviewEntity.builder().id(reviewId).status(ReviewStatus.APPROVED).build();
        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.PENDING);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.updateReviewStatus(reviewId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot revert");
    }

    @Test
    void updateReviewStatus_throws_whenAlreadyRejected() {
        UUID reviewId = UUID.randomUUID();
        ReviewEntity review = ReviewEntity.builder().id(reviewId).status(ReviewStatus.REJECTED).build();
        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.APPROVED);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.updateReviewStatus(reviewId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already rejected");
    }

    @Test
    void updateReviewStatus_succeeds_whenPendingToApproved() {
        UUID reviewId = UUID.randomUUID();
        ReviewEntity review = ReviewEntity.builder().id(reviewId).status(ReviewStatus.PENDING).build();
        ReviewStatusRequest request = new ReviewStatusRequest();
        request.setStatus(ReviewStatus.APPROVED);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        service.updateReviewStatus(reviewId, request);

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.APPROVED);
    }

    @Test
    void updateReviewStatus_throws_whenReviewNotFound() {
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateReviewStatus(reviewId, new ReviewStatusRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}