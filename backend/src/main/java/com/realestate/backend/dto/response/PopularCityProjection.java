package com.realestate.backend.dto.response;

public interface PopularCityProjection {
    String getCityName();
    Long getPropertyCount();
    Long getTotalViews();
}
