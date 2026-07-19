package com.realestate.backend.dto.request;

import com.realestate.backend.enums.ListingType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyMapFilterRequest {

    private String city;
    private ListingType listingType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

}
