package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.enums.Currency;
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
        "currency",
        "listingType",
        "latitude",
        "longitude"
})
public class PropertyMapResponse {

    private UUID id;
    private String title;
    private BigDecimal price;
    private Currency currency;
    private ListingType listingType;
    private BigDecimal latitude;
    private BigDecimal longitude;
//    private String mainImageUrl;

}
