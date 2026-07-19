package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyMemberResponse {

    private UUID id;
    private UUID agencyId;
    private String agencyName;
    private UUID userId;
    private String userFullName;
    private String userEmail;
    private String position;
    private boolean active;

}
