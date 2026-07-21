package com.realestate.backend.dto.request;

import com.realestate.backend.enums.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewStatusRequest {

    private ReviewStatus status;

}
