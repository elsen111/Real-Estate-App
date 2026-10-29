package com.realestate.backend.dto.response;

public record PopularCityResponse(
    String cityName,
    Long propertyCount,
    Long totalViews
) {
}
