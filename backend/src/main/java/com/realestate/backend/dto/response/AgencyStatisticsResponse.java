package com.realestate.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AgencyStatisticsResponse {

    private long totalAgents;
    private long totalProperties;
    private long activeListings;

}
