package com.realestate.backend.dto.admin.agency.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.realestate.backend.enums.AgencyStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminAgencyResponse {

    UUID id;
    String name;
    String description;
    String phoneNumber;
    String email;
    String website;
    String logoUrl;
    String city;
    String address;
    AgencyStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

}
