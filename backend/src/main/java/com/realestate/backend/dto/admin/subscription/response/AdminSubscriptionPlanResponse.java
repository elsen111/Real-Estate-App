package com.realestate.backend.dto.admin.subscription.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSubscriptionPlanResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Integer maxListings;
    private Integer maximumAgents;
    private Boolean featuredListingsAllowed;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
