package com.realestate.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanRequest {

    @NotNull(message = "Name is required")
    @Size(max = 100, message = "Name length should be most 200 characters")
    private String name;

    @NotNull(message = "Description is required")
    @Size(max = 500, message = "Description length should be most 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price should not be negative")
    private BigDecimal price;

    @NotNull(message = "Duration days is required")
    @Positive(message = "The count of duration days should be positive")
    private Integer durationDays;

    @NotNull(message = "Maximum listings is required")
    @Positive(message = "The count of allowed maximum listings should be positive")
    private Integer maxListings;

    @NotNull(message = "The count of allowed maximum agents is required")
    @Positive(message = "The count of allowed maximum agents should be positive")
    private Integer maxAgents;

    private Boolean featuredListingsAllowed;

}
