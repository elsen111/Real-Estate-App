package com.realestate.backend.dto.admin.property.request;

import com.realestate.backend.enums.PropertyStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminPropertyFilterRequest {

    private String query;
    private PropertyStatus status;
    private String city;
    private String agencyName;
    private Boolean featured;

}
