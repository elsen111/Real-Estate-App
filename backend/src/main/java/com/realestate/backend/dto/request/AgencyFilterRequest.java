package com.realestate.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyFilterRequest {

    private String city;
    private String query;

}
