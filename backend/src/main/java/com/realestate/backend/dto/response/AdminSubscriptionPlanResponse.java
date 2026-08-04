package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "price",
        "durationDays",
        "maxListings",
        "maximumAgents",
        "featuredListingsAllowed",
        "active",
        "createdAt",
        "updatedAt"
})
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
