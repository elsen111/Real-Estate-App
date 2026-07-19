package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.realestate.backend.enums.AgencyStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyResponse {

    private UUID id;
    private String name;
    private String description;
    private String email;
    private String phoneNumber;
    private String website;
    private String logoUrl;
    private String city;
    private String address;
    private long totalAgents;
    private AgencyStatus status;

}
