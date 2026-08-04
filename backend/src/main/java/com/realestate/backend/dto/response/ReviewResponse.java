package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "id",
        "status",
        "comment",
        "rating",
        "target",
        "propertyId",
        "reviewerId",
        "reviewerName",
        "reviewerEmail",
        "reviewerAvatarUrl",
        "agencyId",
        "createdAt"
})
public class ReviewResponse {

    private UUID id;
    private ReviewStatus status;
    private String comment;
    private Integer rating;
    private ReviewTargetType target;
    private UUID propertyId;
    private UUID reviewerId;
    private String reviewerName;
    private String reviewerEmail;
    private String reviewerAvatarUrl;
    private UUID agencyId;
    private LocalDateTime createdAt;

}
