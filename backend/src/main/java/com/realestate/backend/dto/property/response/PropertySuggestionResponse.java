package com.realestate.backend.dto.property.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PropertySuggestionResponse {

    private UUID id;
    private String title;

}
