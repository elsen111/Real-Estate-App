package com.realestate.backend.dto.response;

import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReviewResponse {

    private UUID id;
    private UUID reviewerId;
    private UUID propertyId;
    private UUID agencyId;
    private String reviewerName;
    private String reviewerEmail;
    private String reviewerAvatarUrl;
    private Integer rating;
    private String comment;
    private ReviewStatus status;
    private ReviewTargetType target;
    private LocalDateTime createdAt;

}
