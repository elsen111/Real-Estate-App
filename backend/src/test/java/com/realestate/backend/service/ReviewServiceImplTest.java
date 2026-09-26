package com.realestate.backend.service;

import com.realestate.backend.dto.request.PublicReviewFilterRequest;
import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
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

import java.math.BigDecimal;
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

        when(propertyRepository.existsById(propertyId))
                .thenReturn(false);

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

        when(propertyRepository.existsById(propertyId))
                .thenReturn(true);

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(
                                UserEntity.builder()
                                        .id(userId)
                                        .build()
                        )
                );

        when(reviewRepository.existsByReviewerIdAndPropertyId(
                userId,
                propertyId
        )).thenReturn(true);

        assertThatThrownBy(
                () -> service.createPropertyReview(
                        propertyId,
                        new ReviewRequest(),
                        clientUser(userId)
                )
        ).isInstanceOf(ConflictException.class);

        verify(propertyRepository).existsById(propertyId);

        verify(userRepository).findById(userId);

        verify(reviewRepository)
                .existsByReviewerIdAndPropertyId(
                        userId,
                        propertyId
                );
    }

    @Test
    void createPropertyReview_createsReviewAndUpdatesPropertyRating() {

        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Excellent property");

        PropertyEntity property = new PropertyEntity();
        property.setId(propertyId);
        property.setReviewCount(0);
        property.setAverageRating(BigDecimal.ZERO);

        UserEntity user = UserEntity.builder()
                .id(userId)
                .build();

        ReviewEntity createdReview = ReviewEntity.builder()
                .rating(5)
                .comment("Excellent property")
                .property(property)
                .reviewer(user)
                .target(ReviewTargetType.PROPERTY)
                .status(ReviewStatus.APPROVED)
                .build();

        ReviewEntity savedReview = ReviewEntity.builder()
                .id(reviewId)
                .rating(5)
                .comment("Excellent property")
                .property(property)
                .reviewer(user)
                .target(ReviewTargetType.PROPERTY)
                .status(ReviewStatus.APPROVED)
                .build();

        ReviewResponse expectedResponse =
                buildReviewResponse(
                        reviewId,
                        ReviewTargetType.PROPERTY
                );

        when(propertyRepository.existsById(propertyId))
                .thenReturn(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(reviewRepository.existsByReviewerIdAndPropertyId(
                userId,
                propertyId
        )).thenReturn(false);

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(reviewMapper.toEntity(
                request,
                property,
                user,
                null
        )).thenReturn(createdReview);

        when(reviewRepository.saveAndFlush(createdReview))
                .thenReturn(savedReview);

        when(reviewRepository.countByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(3);

        when(reviewRepository.sumRatingByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(new BigDecimal("13"));

        when(reviewMapper.toResponse(savedReview))
                .thenReturn(expectedResponse);

        ReviewResponse result =
                service.createPropertyReview(
                        propertyId,
                        request,
                        clientUser(userId)
                );

        assertThat(result)
                .isEqualTo(expectedResponse);

        assertThat(property.getReviewCount())
                .isEqualTo(3);

        assertThat(property.getAverageRating())
                .isEqualByComparingTo("4.33");

        verify(propertyRepository).existsById(propertyId);

        verify(userRepository).findById(userId);

        verify(reviewRepository)
                .existsByReviewerIdAndPropertyId(
                        userId,
                        propertyId
                );

        verify(propertyRepository).findById(propertyId);

        verify(reviewMapper).toEntity(
                request,
                property,
                user,
                null
        );

        verify(reviewRepository).saveAndFlush(createdReview);

        verify(reviewRepository).countByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        );

        verify(reviewRepository).sumRatingByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        );

        verify(reviewMapper).toResponse(savedReview);
    }

    @Test
    void createPropertyReview_setsZeroRating_whenNoApprovedReviews() {

        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        PropertyEntity property = new PropertyEntity();
        property.setId(propertyId);
        property.setReviewCount(5);
        property.setAverageRating(new BigDecimal("4.50"));

        UserEntity user = UserEntity.builder()
                .id(userId)
                .build();

        ReviewEntity createdReview = ReviewEntity.builder()
                .rating(5)
                .property(property)
                .reviewer(user)
                .target(ReviewTargetType.PROPERTY)
                .status(ReviewStatus.APPROVED)
                .build();

        ReviewEntity savedReview = ReviewEntity.builder()
                .id(reviewId)
                .rating(5)
                .property(property)
                .reviewer(user)
                .target(ReviewTargetType.PROPERTY)
                .status(ReviewStatus.APPROVED)
                .build();

        ReviewResponse response =
                buildReviewResponse(
                        reviewId,
                        ReviewTargetType.PROPERTY
                );

        when(propertyRepository.existsById(propertyId))
                .thenReturn(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(reviewRepository.existsByReviewerIdAndPropertyId(
                userId,
                propertyId
        )).thenReturn(false);

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(reviewMapper.toEntity(
                any(ReviewRequest.class),
                eq(property),
                eq(user),
                eq(null)
        )).thenReturn(createdReview);

        when(reviewRepository.saveAndFlush(createdReview))
                .thenReturn(savedReview);

        when(reviewRepository.countByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(0);

        when(reviewRepository.sumRatingByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(BigDecimal.ZERO);

        when(reviewMapper.toResponse(savedReview))
                .thenReturn(response);

        service.createPropertyReview(
                propertyId,
                new ReviewRequest(),
                clientUser(userId)
        );

        assertThat(property.getReviewCount())
                .isZero();

        assertThat(property.getAverageRating())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getPropertyReviews_throws_whenPropertyNotFound() {

        UUID propertyId = UUID.randomUUID();

        PublicReviewFilterRequest filter =
                new PublicReviewFilterRequest(5);

        Pageable pageable = Pageable.ofSize(10);

        when(propertyRepository.existsById(propertyId))
                .thenReturn(false);

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

        ReviewEntity review =
                buildPropertyReview(reviewId);

        ReviewResponse expectedResponse =
                buildReviewResponse(
                        reviewId,
                        ReviewTargetType.PROPERTY
                );

        Page<ReviewEntity> reviewPage =
                new PageImpl<>(List.of(review));

        when(propertyRepository.existsById(propertyId))
                .thenReturn(true);

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

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().getFirst())
                .isEqualTo(expectedResponse);

        verify(propertyRepository)
                .existsById(propertyId);

        verify(reviewRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(reviewMapper)
                .toResponse(review);
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

        PublicReviewFilterRequest filter =
                new PublicReviewFilterRequest(5);

        Pageable pageable = Pageable.ofSize(10);

        when(agencyRepository.existsById(agencyId))
                .thenReturn(false);

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

        ReviewEntity review =
                buildAgencyReview(reviewId);

        ReviewResponse expectedResponse =
                buildReviewResponse(
                        reviewId,
                        ReviewTargetType.AGENCY
                );

        Page<ReviewEntity> reviewPage =
                new PageImpl<>(List.of(review));

        when(agencyRepository.existsById(agencyId))
                .thenReturn(true);

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

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().getFirst())
                .isEqualTo(expectedResponse);

        verify(agencyRepository)
                .existsById(agencyId);

        verify(reviewRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(reviewMapper)
                .toResponse(review);
    }

    @Test
    void updateOwnReview_throws_whenReviewDoesNotBelongToCaller() {

        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(reviewRepository.existsByIdAndReviewerId(
                reviewId,
                userId
        )).thenReturn(false);

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

        when(reviewRepository.findByIdAndReviewerId(
                reviewId,
                userId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.deleteOwnReview(
                        reviewId,
                        clientUser(userId)
                )
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteOwnReview_updatesPropertyRating_whenApprovedPropertyReviewIsDeleted() {

        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        PropertyEntity property = new PropertyEntity();
        property.setId(propertyId);
        property.setReviewCount(3);
        property.setAverageRating(new BigDecimal("4.33"));

        ReviewEntity review = ReviewEntity.builder()
                .id(reviewId)
                .reviewer(
                        UserEntity.builder()
                                .id(userId)
                                .build()
                )
                .property(property)
                .target(ReviewTargetType.PROPERTY)
                .status(ReviewStatus.APPROVED)
                .rating(5)
                .build();

        when(reviewRepository.findByIdAndReviewerId(
                reviewId,
                userId
        )).thenReturn(Optional.of(review));

        when(reviewRepository.countByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(2);

        when(reviewRepository.sumRatingByPropertyIdAndStatus(
                propertyId,
                ReviewStatus.APPROVED
        )).thenReturn(new BigDecimal("8"));

        service.deleteOwnReview(
                reviewId,
                clientUser(userId)
        );

        assertThat(property.getReviewCount())
                .isEqualTo(2);

        assertThat(property.getAverageRating())
                .isEqualByComparingTo("4.00");

        verify(reviewRepository)
                .findByIdAndReviewerId(reviewId, userId);

        verify(reviewRepository)
                .delete(review);

        verify(reviewRepository)
                .flush();

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