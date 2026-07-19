package com.realestate.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyAgentFilterRequest {

    private String query;
    private Boolean enabled;

}
