package com.realestate.backend.dto.request;

import com.realestate.backend.enums.ListingType;
import com.realestate.backend.enums.PropertyType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyPublicFilterRequest {

    private String query;
    private String city;
    private String district;
    private String agencyName;
    private PropertyType propertyType;
    private ListingType listingType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer roomCount;
    private BigDecimal minArea;
    private BigDecimal maxArea;
    private Boolean featured;

}
