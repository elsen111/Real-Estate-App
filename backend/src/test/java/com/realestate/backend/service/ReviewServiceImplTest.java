package com.realestate.backend.service;

import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private AgencyRepository agencyRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ReviewServiceImpl service;

    private CustomUserDetails clientUser(UUID id) {
        return CustomUserDetails.from(UserEntity.builder().id(id)
                .roles(Set.of(RoleEntity.builder().roleName(Role.CLIENT).build())).build());
    }

    @Test
    void createPropertyReview_throws_whenCallerIsNotClient() {
        CustomUserDetails agent = CustomUserDetails.from(UserEntity.builder().id(UUID.randomUUID())
                .roles(Set.of(RoleEntity.builder().roleName(Role.AGENT).build())).build());

        assertThatThrownBy(() -> service.createPropertyReview(UUID.randomUUID(), new ReviewRequest(), agent))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createPropertyReview_throws_whenPropertyNotFound() {
        UUID propertyId = UUID.randomUUID();
        when(propertyRepository.existsById(propertyId)).thenReturn(false);

        assertThatThrownBy(() -> service.createPropertyReview(propertyId, new ReviewRequest(), clientUser(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createPropertyReview_throws_whenUserAlreadyReviewedProperty() {
        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(propertyRepository.existsById(propertyId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserEntity.builder().id(userId).build()));
        when(reviewRepository.existsByReviewerIdAndPropertyId(userId, propertyId)).thenReturn(true);

        assertThatThrownBy(() -> service.createPropertyReview(propertyId, new ReviewRequest(), clientUser(userId)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void createAgencyReview_throws_whenAgencyNotFound() {
        UUID agencyId = UUID.randomUUID();
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAgencyReview(agencyId, new ReviewRequest(), clientUser(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateOwnReview_throws_whenReviewDoesNotBelongToCaller() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(reviewRepository.existsByIdAndReviewerId(reviewId, userId)).thenReturn(false);

        assertThatThrownBy(() -> service.updateOwnReview(reviewId, new ReviewRequest(), clientUser(userId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteOwnReview_throws_whenReviewNotFoundForCaller() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(reviewRepository.findByIdAndReviewerId(reviewId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteOwnReview(reviewId, clientUser(userId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}