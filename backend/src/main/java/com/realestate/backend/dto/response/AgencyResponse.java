package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.enums.AgencyStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "logoUrl",
        "email",
        "phoneNumber",
        "website",
        "city",
        "address",
        "totalAgents",
        "status"
})
public class AgencyResponse {

    private UUID id;
    private String name;
    private String description;
    private String logoUrl;
    private String email;
    private String phoneNumber;
    private String website;
    private String city;
    private String address;
    private Long totalAgents;
    private AgencyStatus status;

}
