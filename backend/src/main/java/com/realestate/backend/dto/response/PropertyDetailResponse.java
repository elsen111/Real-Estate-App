package com.realestate.backend.dto.response;

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
@Builder(toBuilder = true)
public class PropertyDetailResponse {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private String city;
    private String district;
    private String address;
    private String propertyType;
    private ListingType listingType;
    private BigDecimal area;
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    private Integer totalFloors;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private PropertyStatus propertyStatus;
    private Boolean featured;
    private List<PropertyMediaResponse> images;
    private AgencyResponse agency;
    private AgentResponse agent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
