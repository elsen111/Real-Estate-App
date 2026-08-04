package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "status",
        "listingType",
        "price",
        "featured",
        "city",
        "district",
        "address",
        "area",
        "rooms",
        "bathrooms",
        "floor",
        "totalFloors",
        "viewCount",
        "createdAt"
})
public class AdminAgencyPropertyResponse {

    private UUID id;
    private String title;
    private String description;
    private PropertyStatus status;
    private ListingType listingType;
    private BigDecimal price;
    private boolean featured;
    private String city;
    private String district;
    private String address;
    private BigDecimal area;
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    private Integer totalFloors;
    private Long viewCount;
    private LocalDateTime createdAt;

}