package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.enums.Currency;
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
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "price",
        "currency",
        "listingType",
        "featured",
        "area",
        "rooms",
        "bathrooms",
        "floor",
        "totalFloors",
        "city",
        "district",
        "address",
        "categoryId",
        "categoryName",
        "images",
        "mainImageUrl",
        "viewCount",
        "agencyId",
        "agencyName",
        "assignedAgentId",
        "assignedAgentName",
        "status",
        "createdAt",
        "updatedAt"
})
public class PropertyResponse {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private Currency currency;
    private ListingType listingType;
    private boolean featured;
    private BigDecimal area;
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    private Integer totalFloors;
    private String city;
    private String district;
    private String address;
    private UUID categoryId;
    private String categoryName;
    private List<PropertyMediaResponse> images;
    private String mainImageUrl;
    private Long viewCount;
    private UUID agencyId;
    private String agencyName;
    private UUID assignedAgentId;
    private String assignedAgentName;
    private PropertyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}