package com.realestate.backend.dto.request;

import com.realestate.backend.enums.PropertyStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyPropertyFilterRequest {

    private String query;
    private PropertyStatus status;
    private String city;
    private Boolean featured;

}

