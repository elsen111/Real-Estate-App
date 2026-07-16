package com.realestate.backend.dto.subscription.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SubscriptionPlanResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Integer maxListings;
    private Integer maxAgents;
    private Boolean featuredListingsAllowed;

}
