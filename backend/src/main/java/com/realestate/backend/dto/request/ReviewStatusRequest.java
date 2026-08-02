package com.realestate.backend.dto.request;

import com.realestate.backend.enums.ReviewStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewStatusRequest {

    private ReviewStatus status;

}
