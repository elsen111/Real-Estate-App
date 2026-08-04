package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@JsonPropertyOrder({
        "id",
        "planId",
        "planName",
        "subscriptionStatus",
        "startDate",
        "endDate",
        "durationDays",
        "price",
        "maxListings",
        "usedListings",
        "remainingListings",
        "maxAgents",
        "usedAgents",
        "remainingAgents"
})
public class AgencySubscriptionResponse {

    private UUID id;
    private UUID planId;
    private String planName;
    private SubscriptionStatus subscriptionStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationDays;
    private BigDecimal price;
    private Integer maxListings;
    private Integer usedListings;
    private Integer remainingListings;
    private Integer maxAgents;
    private Integer usedAgents;
    private Integer remainingAgents;

}
