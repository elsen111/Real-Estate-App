package com.realestate.backend.dto.request;

import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReviewFilterRequest {

    private ReviewStatus status;
    private ReviewTargetType targetType;
    private String agencyName;
    private String propertyTitle;

}
