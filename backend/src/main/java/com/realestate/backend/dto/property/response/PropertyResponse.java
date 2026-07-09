package com.realestate.backend.dto.property.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.realestate.backend.enums.ListingType;
import com.realestate.backend.enums.PropertyStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyResponse {

    private UUID id;

    private String title;

    private String description;

    private String agencyName;

    private BigDecimal price;

    private String city;

    private String district;

    private String address;

    private ListingType listingType;

    private BigDecimal area;

    private Integer rooms;

    private Integer bathrooms;

    private Integer floor;

    private Integer totalFloors;

    private PropertyStatus status;

    private boolean featured;

    private Long viewCount;

    private LocalDateTime createdAt;
}