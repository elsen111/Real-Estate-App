package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewStatusRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import com.realestate.backend.service.AdminReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReviewControllerTest {

    @Mock
    private AdminReviewService adminReviewService;

    @InjectMocks
    private AdminReviewController controller;

    @Test
    void getAllReviews_returnsOkWithPagedReviews() {
        AdminReviewFilterRequest filter = AdminReviewFilterRequest.builder()
                .status(ReviewStatus.PENDING)
                .targetType(ReviewTargetType.PROPERTY)
                .build();
        Pageable pageable = Pageable.ofSize(10);

        ReviewResponse review = ReviewResponse.builder()
                .id(UUID.randomUUID())
                .reviewerName("John Doe")
                .rating(5)
                .status(ReviewStatus.PENDING)
                .target(ReviewTargetType.PROPERTY)
                .build();
        Page<ReviewResponse> page = new PageImpl<>(List.of(review));

        when(adminReviewService.getAllReviews(filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<ReviewResponse>>> response =
                controller.getAllReviews(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Reviews fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(page);
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(adminReviewService).getAllReviews(filter, pageable);
        verifyNoMoreInteractions(adminReviewService);
    }

    @Test
    void getAllReviews_returnsOkWithEmptyPage_whenNoReviewsMatchFilter() {
        AdminReviewFilterRequest filter = AdminReviewFilterRequest.builder().build();
        Pageable pageable = Pageable.ofSize(10);
        Page<ReviewResponse> emptyPage = new PageImpl<>(List.of());

        when(adminReviewService.getAllReviews(any(), any())).thenReturn(emptyPage);

        ResponseEntity<ApiResponse<Page<ReviewResponse>>> response =
                controller.getAllReviews(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).isEmpty();
    }

    @Test
    void updateReviewStatus_returnsOk_whenStatusUpdatedSuccessfully() {
        UUID reviewId = UUID.randomUUID();
        ReviewStatusRequest request = ReviewStatusRequest.builder()
                .status(ReviewStatus.APPROVED)
                .build();

        ResponseEntity<ApiResponse<Void>> response =
                controller.updateReviewStatus(reviewId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Review status changed successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(adminReviewService).updateReviewStatus(eq(reviewId), eq(request));
        verifyNoMoreInteractions(adminReviewService);
    }

    @Test
    void updateReviewStatus_propagatesException_whenServiceThrows() {
        UUID reviewId = UUID.randomUUID();
        ReviewStatusRequest request = ReviewStatusRequest.builder()
                .status(ReviewStatus.REJECTED)
                .build();

        org.mockito.Mockito.doThrow(new RuntimeException("Review not found"))
                .when(adminReviewService).updateReviewStatus(reviewId, request);

        try {
            controller.updateReviewStatus(reviewId, request);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Review not found");
        }

        verify(adminReviewService).updateReviewStatus(reviewId, request);
    }
}