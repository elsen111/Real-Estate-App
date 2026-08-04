package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.enums.ListingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "id",
        "title",
        "price",
        "listingType",
        "latitude",
        "longitude"
})
public class PropertyMapResponse {

    private UUID id;
    private String title;
    private BigDecimal price;
    private ListingType listingType;
    private BigDecimal latitude;
    private BigDecimal longitude;
//    private String mainImageUrl;

}
