package com.realestate.backend.dto.agency.response;

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
public class AgencySubscriptionResponse {

    private UUID id;
    private UUID planId;
    private String planName;
    private BigDecimal price;
    private Integer durationDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus subscriptionStatus;
    private Integer maxListings;
    private Integer usedListings;
    private Integer remainingListings;
    private Integer maxAgents;
    private Integer usedAgents;
    private Integer remainingAgents;

}
