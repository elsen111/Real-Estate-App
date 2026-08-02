package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.ReviewStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReviewMapperTest {

    private ReviewMapper reviewMapper;

    @BeforeEach
    void setup() {
        reviewMapper = Mappers.getMapper(ReviewMapper.class);
    }

    @Test
    void shouldMapToEntity() {

        ReviewRequest request = createReviewRequest();
        UUID propertyId = UUID.randomUUID();
        UserEntity reviewer = createReviewer();
        AgencyEntity agency = createAgency();

        ReviewEntity entity =
                reviewMapper.toEntity(request, propertyId, reviewer, agency);

        assertNotNull(entity);

        assertEquals(request.getRating(), entity.getRating());
        assertEquals(request.getComment(), entity.getComment());

        assertEquals(ReviewStatus.APPROVED, entity.getStatus());        assertNull(entity.getTarget());

        assertEquals(propertyId, entity.getProperty().getId());

        assertEquals(reviewer, entity.getReviewer());
        assertEquals(agency, entity.getAgency());

        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void shouldMapToResponse() {

        ReviewEntity entity = createReviewEntity();

        ReviewResponse response = reviewMapper.toResponse(entity);

        assertNotNull(response);

        assertEquals(entity.getId(), response.getId());

        assertEquals(
                entity.getProperty().getId(),
                response.getPropertyId()
        );

        assertEquals(
                entity.getReviewer().getId(),
                response.getReviewerId()
        );

        assertEquals(
                entity.getAgency().getId(),
                response.getAgencyId()
        );

        assertEquals(
                entity.getReviewer().getFullName(),
                response.getReviewerName()
        );

        assertEquals(
                entity.getReviewer().getEmail(),
                response.getReviewerEmail()
        );

        assertEquals(
                entity.getReviewer()
                        .getProfilePhotoUrl()
                        .getMedia()
                        .getFileUrl(),
                response.getReviewerAvatarUrl()
        );

        assertEquals(entity.getRating(), response.getRating());
        assertEquals(entity.getComment(), response.getComment());
    }

    @Test
    void shouldUpdateEntity() {

        ReviewRequest request = createUpdatedReviewRequest();

        UserEntity reviewer = createReviewer();

        ReviewEntity entity = createReviewEntity();

        UUID id = entity.getId();
        PropertyEntity property = entity.getProperty();
        AgencyEntity agency = entity.getAgency();
        LocalDateTime createdAt = entity.getCreatedAt();
        LocalDateTime updatedAt = entity.getUpdatedAt();
        Object status = entity.getStatus();
        Object target = entity.getTarget();

        reviewMapper.toEntity(request, reviewer, entity);

        assertEquals(request.getRating(), entity.getRating());
        assertEquals(request.getComment(), entity.getComment());

        assertEquals(reviewer, entity.getReviewer());

        assertEquals(id, entity.getId());
        assertEquals(property, entity.getProperty());
        assertEquals(agency, entity.getAgency());
        assertEquals(status, entity.getStatus());
        assertEquals(target, entity.getTarget());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }

    // Helpers
    private ReviewEntity createReviewEntity() {

        PropertyEntity property = new PropertyEntity();
        property.setId(UUID.randomUUID());

        MediaFileEntity mediaFile = new MediaFileEntity();
        mediaFile.setFileUrl("https://cdn.test/avatar.png");

        UserMediaEntity userMedia = new UserMediaEntity();
        userMedia.setMedia(mediaFile);

        UserEntity reviewer = createReviewer();
        reviewer.setProfilePhotoUrl(userMedia);

        AgencyEntity agency = createAgency();

        ReviewEntity review = new ReviewEntity();
        review.setId(UUID.randomUUID());
        review.setRating(5);
        review.setComment("Excellent service");
        review.setProperty(property);
        review.setReviewer(reviewer);
        review.setAgency(agency);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        return review;
    }

    private UserEntity createReviewer() {

        UserEntity reviewer = new UserEntity();
        reviewer.setId(UUID.randomUUID());
        reviewer.setFullName("John Smith");
        reviewer.setEmail("john@gmail.com");

        return reviewer;
    }

    private AgencyEntity createAgency() {

        AgencyEntity agency = new AgencyEntity();
        agency.setId(UUID.randomUUID());
        agency.setName("Dream Estate");

        return agency;
    }

    private ReviewRequest createReviewRequest() {

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Excellent service");

        return request;
    }

    private ReviewRequest createUpdatedReviewRequest() {

        ReviewRequest request = new ReviewRequest();
        request.setRating(4);
        request.setComment("Updated review");

        return request;
    }
}