package com.realestate.backend.dto.agency.response;

import com.realestate.backend.enums.AgencyStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AgencyResponse {

    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String city;
    private String address;
    private AgencyStatus status;

}
