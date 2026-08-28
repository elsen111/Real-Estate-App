package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.PublicReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.ratelimit.RateLimitFilter;
import com.realestate.backend.service.ReviewService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private RateLimitFilter rateLimitFilter;

    @InjectMocks
    private ReviewController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    private ReviewRequest buildValidRequest() {
        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Great experience, highly recommended!");
        return request;
    }

    private PublicReviewFilterRequest buildPublicFilter(Integer rating) {
        PublicReviewFilterRequest filter = new PublicReviewFilterRequest(rating);
        return filter;
    }

    private ReviewResponse buildReviewResponse(UUID id, ReviewTargetType target) {
        return ReviewResponse.builder()
                .id(id)
                .reviewerName("Jane Doe")
                .rating(5)
                .comment("Great experience, highly recommended!")
                .status(ReviewStatus.PENDING)
                .target(target)
                .build();
    }

    @Test
    void createPropertyReview_returnsOk_withCreatedReview() {
        UUID propertyId = UUID.randomUUID();
        ReviewRequest request = buildValidRequest();
        ReviewResponse expected = buildReviewResponse(
                UUID.randomUUID(),
                ReviewTargetType.PROPERTY
        );

        when(reviewService.createPropertyReview(propertyId, request, currentUser))
                .thenReturn(expected);

        ResponseEntity<ApiResponse<ReviewResponse>> response =
                controller.createPropertyReview(propertyId, request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Review created successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(reviewService)
                .createPropertyReview(propertyId, request, currentUser);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void createPropertyReview_propagatesException_whenAlreadyReviewed() {
        UUID propertyId = UUID.randomUUID();
        ReviewRequest request = buildValidRequest();

        when(reviewService.createPropertyReview(propertyId, request, currentUser))
                .thenThrow(
                        new RuntimeException(
                                "You have already reviewed this property"
                        )
                );

        try {
            controller.createPropertyReview(propertyId, request, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage())
                    .isEqualTo("You have already reviewed this property");
        }

        verify(reviewService)
                .createPropertyReview(propertyId, request, currentUser);
    }

    @Test
    void getPropertyReviews_returnsOk_withReviewPage() {
        UUID propertyId = UUID.randomUUID();
        PublicReviewFilterRequest filter = buildPublicFilter(5);
        Pageable pageable = Pageable.ofSize(10);

        Page<ReviewResponse> page = new PageImpl<>(
                List.of(
                        buildReviewResponse(
                                UUID.randomUUID(),
                                ReviewTargetType.PROPERTY
                        )
                )
        );

        when(reviewService.getPropertyReviews(propertyId, filter, pageable))
                .thenReturn(page);

        ResponseEntity<ApiResponse<Page<ReviewResponse>>> response =
                controller.getPropertyReviews(propertyId, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Property review list fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(reviewService)
                .getPropertyReviews(propertyId, filter, pageable);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void getPropertyReviews_returnsOk_withEmptyPage_whenNoReviews() {
        UUID propertyId = UUID.randomUUID();
        PublicReviewFilterRequest filter = buildPublicFilter(null);
        Pageable pageable = Pageable.ofSize(10);

        Page<ReviewResponse> emptyPage = new PageImpl<>(List.of());

        when(reviewService.getPropertyReviews(propertyId, filter, pageable))
                .thenReturn(emptyPage);

        ResponseEntity<ApiResponse<Page<ReviewResponse>>> response =
                controller.getPropertyReviews(propertyId, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getContent()).isEmpty();

        verify(reviewService)
                .getPropertyReviews(propertyId, filter, pageable);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void createAgencyReview_returnsOk_withCreatedReview() {
        UUID agencyId = UUID.randomUUID();
        ReviewRequest request = buildValidRequest();
        ReviewResponse expected = buildReviewResponse(
                UUID.randomUUID(),
                ReviewTargetType.AGENCY
        );

        when(reviewService.createAgencyReview(agencyId, request, currentUser))
                .thenReturn(expected);

        ResponseEntity<ApiResponse<ReviewResponse>> response =
                controller.createAgencyReview(agencyId, request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Review created successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(reviewService)
                .createAgencyReview(agencyId, request, currentUser);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void getAgencyReviews_returnsOk_withReviewPage() {
        UUID agencyId = UUID.randomUUID();
        PublicReviewFilterRequest filter = buildPublicFilter(5);
        Pageable pageable = Pageable.ofSize(10);

        Page<ReviewResponse> page = new PageImpl<>(
                List.of(
                        buildReviewResponse(
                                UUID.randomUUID(),
                                ReviewTargetType.AGENCY
                        )
                )
        );

        when(reviewService.getAgencyReviews(agencyId, filter, pageable))
                .thenReturn(page);

        ResponseEntity<ApiResponse<Page<ReviewResponse>>> response =
                controller.getAgencyReviews(agencyId, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Agency review list fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(reviewService)
                .getAgencyReviews(agencyId, filter, pageable);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void getAgencyReviews_returnsOk_withEmptyPage_whenNoReviews() {
        UUID agencyId = UUID.randomUUID();
        PublicReviewFilterRequest filter = buildPublicFilter(null);
        Pageable pageable = Pageable.ofSize(10);

        Page<ReviewResponse> emptyPage = new PageImpl<>(List.of());

        when(reviewService.getAgencyReviews(agencyId, filter, pageable))
                .thenReturn(emptyPage);

        ResponseEntity<ApiResponse<Page<ReviewResponse>>> response =
                controller.getAgencyReviews(agencyId, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getContent()).isEmpty();

        verify(reviewService)
                .getAgencyReviews(agencyId, filter, pageable);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void updateOwnReview_returnsOk_withUpdatedReview() {
        UUID reviewId = UUID.randomUUID();
        ReviewRequest request = buildValidRequest();
        ReviewResponse expected = buildReviewResponse(
                reviewId,
                ReviewTargetType.PROPERTY
        );

        when(reviewService.updateOwnReview(reviewId, request, currentUser))
                .thenReturn(expected);

        ResponseEntity<ApiResponse<ReviewResponse>> response =
                controller.updateOwnReview(reviewId, request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Review updated successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(reviewService)
                .updateOwnReview(reviewId, request, currentUser);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void updateOwnReview_propagatesException_whenNotOwner() {
        UUID reviewId = UUID.randomUUID();
        ReviewRequest request = buildValidRequest();

        when(reviewService.updateOwnReview(reviewId, request, currentUser))
                .thenThrow(
                        new RuntimeException(
                                "You are not allowed to update this review"
                        )
                );

        try {
            controller.updateOwnReview(reviewId, request, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage())
                    .isEqualTo("You are not allowed to update this review");
        }

        verify(reviewService)
                .updateOwnReview(reviewId, request, currentUser);
    }

    @Test
    void deleteOwnReview_returnsOk_withNoData() {
        UUID reviewId = UUID.randomUUID();

        ResponseEntity<ApiResponse<Void>> response =
                controller.deleteOwnReview(reviewId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Review deleted successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(reviewService).deleteOwnReview(reviewId, currentUser);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void deleteOwnReview_propagatesException_whenReviewNotFound() {
        UUID reviewId = UUID.randomUUID();

        doThrow(new RuntimeException("Review not found"))
                .when(reviewService)
                .deleteOwnReview(reviewId, currentUser);

        try {
            controller.deleteOwnReview(reviewId, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Review not found");
        }

        verify(reviewService).deleteOwnReview(reviewId, currentUser);
    }
}