package com.realestate.backend.dto.agency.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyFilterRequest {

    private String city;
    private String query;

}
