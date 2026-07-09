package com.realestate.backend.dto.agency.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyAgentFilterRequest {

    private String query;
    private Boolean enabled;

}
