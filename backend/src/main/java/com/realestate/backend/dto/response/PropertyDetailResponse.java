package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "price",
        "listingType",
        "propertyType",
        "featured",
        "area",
        "rooms",
        "bathrooms",
        "floor",
        "totalFloors",
        "city",
        "district",
        "address",
        "latitude",
        "longitude",
        "images",
        "agency",
        "agent",
        "propertyStatus",
        "createdAt",
        "updatedAt"
})
public class PropertyDetailResponse {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private ListingType listingType;
    private String propertyType;
    private Boolean featured;
    private BigDecimal area;
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    private Integer totalFloors;
    private String city;
    private String district;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<PropertyMediaResponse> images;
    private AgencyResponse agency;
    private AgentResponse agent;
    private PropertyStatus propertyStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
