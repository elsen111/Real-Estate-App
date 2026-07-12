package com.realestate.backend.dto.property.response;

import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.dto.agent.response.AgentResponse;
import com.realestate.backend.dto.media.response.PropertyImageResponse;
import com.realestate.backend.enums.ListingType;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.PropertyType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
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
    private List<PropertyImageResponse> images;
    private AgencyResponse agency;
    private AgentResponse agent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
