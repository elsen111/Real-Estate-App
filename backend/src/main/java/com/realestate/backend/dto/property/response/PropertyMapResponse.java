package com.realestate.backend.dto.property.response;

import com.realestate.backend.enums.ListingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class PropertyMapResponse {

    private UUID id;
    private String title;
    private BigDecimal price;
    private ListingType listingType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String mainImageUrl;

}
