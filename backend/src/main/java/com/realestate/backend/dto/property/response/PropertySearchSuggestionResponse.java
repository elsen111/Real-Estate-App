package com.realestate.backend.dto.property.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PropertySearchSuggestionResponse {

    private List<PropertySuggestionResponse> properties;
    private List<String> cities;
    private List<String> districts;

}
