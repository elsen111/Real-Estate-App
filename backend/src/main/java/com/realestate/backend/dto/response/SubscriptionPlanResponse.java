package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "price",
        "durationDays",
        "maxListings",
        "maxAgents",
        "featuredListingsAllowed"
})
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
