package com.realestate.backend.dto.admin.property.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.realestate.backend.dto.property.response.PropertyMediaResponse;
import com.realestate.backend.enums.ListingType;
import com.realestate.backend.enums.PropertyStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminAgencyPropertyResponse {

    private UUID id;
    private String title;
    private String description;
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