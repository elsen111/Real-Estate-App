package com.realestate.backend.service;

import com.realestate.backend.dto.request.PublicReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
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
import com.realestate.backend.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl service;

    private CustomUserDetails clientUser(UUID id) {
        return CustomUserDetails.from(
                UserEntity.builder()
                        .id(id)
                        .roles(
                                Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.CLIENT)
                                                .build()
                                )
                        )
                        .build()
        );
    }

    private ReviewEntity buildPropertyReview(UUID reviewId) {
        return ReviewEntity.builder()
                .id(reviewId)
                .rating(5)
                .comment("Great property")
                .status(ReviewStatus.APPROVED)
                .target(ReviewTargetType.PROPERTY)
                .build();
    }

    private ReviewEntity buildAgencyReview(UUID reviewId) {
        return ReviewEntity.builder()
                .id(reviewId)
                .rating(5)
                .comment("Great agency")
                .status(ReviewStatus.APPROVED)
                .target(ReviewTargetType.AGENCY)
                .build();
    }

    private ReviewResponse buildReviewResponse(
            UUID reviewId,
            ReviewTargetType target
    ) {
        return ReviewResponse.builder()
                .id(reviewId)
                .rating(5)
                .comment("Great review")
                .status(ReviewStatus.APPROVED)
                .target(target)
                .build();
    }

    @Test
    void createPropertyReview_throws_whenCallerIsNotClient() {
        CustomUserDetails agent = CustomUserDetails.from(
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .roles(
                                Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.AGENT)
                                                .build()
                                )
                        )
                        .build()
        );

        assertThatThrownBy(
                () -> service.createPropertyReview(
                        UUID.randomUUID(),
                        new ReviewRequest(),
                        agent
                )
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createPropertyReview_throws_whenPropertyNotFound() {
        UUID propertyId = UUID.randomUUID();

        when(propertyRepository.existsById(propertyId)).thenReturn(false);

        assertThatThrownBy(
                () -> service.createPropertyReview(
                        propertyId,
                        new ReviewRequest(),
                        clientUser(UUID.randomUUID())
                )
        ).isInstanceOf(ResourceNotFoundException.class);

        verify(propertyRepository).existsById(propertyId);
    }

    @Test
    void createPropertyReview_throws_whenUserAlreadyReviewedProperty() {
        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(propertyRepository.existsById(propertyId)).thenReturn(true);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(UserEntity.builder().id(userId).build()));
        when(reviewRepository.existsByReviewerIdAndPropertyId(userId, propertyId))
                .thenReturn(true);

        assertThatThrownBy(
                () -> service.createPropertyReview(
                        propertyId,
                        new ReviewRequest(),
                        clientUser(userId)
                )
        ).isInstanceOf(BusinessException.class);

        verify(propertyRepository).existsById(propertyId);
        verify(userRepository).findById(userId);
        verify(reviewRepository)
                .existsByReviewerIdAndPropertyId(userId, propertyId);
    }

    @Test
    void getPropertyReviews_throws_whenPropertyNotFound() {
        UUID propertyId = UUID.randomUUID();
        PublicReviewFilterRequest filter = new PublicReviewFilterRequest(5);
        Pageable pageable = Pageable.ofSize(10);

        when(propertyRepository.existsById(propertyId)).thenReturn(false);

        assertThatThrownBy(
                () -> service.getPropertyReviews(
                        propertyId,
                        filter,
                        pageable
                )
        ).isInstanceOf(ResourceNotFoundException.class);

        verify(propertyRepository).existsById(propertyId);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    void getPropertyReviews_returnsMappedReviewPage() {
        UUID propertyId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        PublicReviewFilterRequest filter =
                new PublicReviewFilterRequest(5);

        Pageable pageable = Pageable.ofSize(10);

        ReviewEntity review = buildPropertyReview(reviewId);

        ReviewResponse expectedResponse =
                buildReviewResponse(reviewId, ReviewTargetType.PROPERTY);

        Page<ReviewEntity> reviewPage =
                new PageImpl<>(List.of(review));

        when(propertyRepository.existsById(propertyId)).thenReturn(true);

        when(reviewRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(reviewPage);

        when(reviewMapper.toResponse(review))
                .thenReturn(expectedResponse);

        Page<ReviewResponse> result =
                service.getPropertyReviews(
                        propertyId,
                        filter,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst())
                .isEqualTo(expectedResponse);

        verify(propertyRepository).existsById(propertyId);

        verify(reviewRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(reviewMapper).toResponse(review);
    }

    @Test
    void createAgencyReview_throws_whenAgencyNotFound() {
        UUID agencyId = UUID.randomUUID();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.createAgencyReview(
                        agencyId,
                        new ReviewRequest(),
                        clientUser(UUID.randomUUID())
                )
        ).isInstanceOf(ResourceNotFoundException.class);

        verify(agencyRepository).findById(agencyId);
    }

    @Test
    void getAgencyReviews_throws_whenAgencyNotFound() {
        UUID agencyId = UUID.randomUUID();
        PublicReviewFilterRequest filter = new PublicReviewFilterRequest(5);
        Pageable pageable = Pageable.ofSize(10);

        when(agencyRepository.existsById(agencyId)).thenReturn(false);

        assertThatThrownBy(
                () -> service.getAgencyReviews(
                        agencyId,
                        filter,
                        pageable
                )
        ).isInstanceOf(ResourceNotFoundException.class);

        verify(agencyRepository).existsById(agencyId);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    void getAgencyReviews_returnsMappedReviewPage() {
        UUID agencyId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        PublicReviewFilterRequest filter =
                new PublicReviewFilterRequest(5);

        Pageable pageable = Pageable.ofSize(10);

        ReviewEntity review = buildAgencyReview(reviewId);

        ReviewResponse expectedResponse =
                buildReviewResponse(reviewId, ReviewTargetType.AGENCY);

        Page<ReviewEntity> reviewPage =
                new PageImpl<>(List.of(review));

        when(agencyRepository.existsById(agencyId)).thenReturn(true);

        when(reviewRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(reviewPage);

        when(reviewMapper.toResponse(review))
                .thenReturn(expectedResponse);

        Page<ReviewResponse> result =
                service.getAgencyReviews(
                        agencyId,
                        filter,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst())
                .isEqualTo(expectedResponse);

        verify(agencyRepository).existsById(agencyId);

        verify(reviewRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(reviewMapper).toResponse(review);
    }

    @Test
    void updateOwnReview_throws_whenReviewDoesNotBelongToCaller() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(reviewRepository.existsByIdAndReviewerId(reviewId, userId))
                .thenReturn(false);

        assertThatThrownBy(
                () -> service.updateOwnReview(
                        reviewId,
                        new ReviewRequest(),
                        clientUser(userId)
                )
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteOwnReview_throws_whenReviewNotFoundForCaller() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(reviewRepository.findByIdAndReviewerId(reviewId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.deleteOwnReview(
                        reviewId,
                        clientUser(userId)
                )
        ).isInstanceOf(ResourceNotFoundException.class);
    }
}