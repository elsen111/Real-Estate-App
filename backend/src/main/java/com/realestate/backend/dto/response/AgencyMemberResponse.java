package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "agencyId",
        "agencyName",
        "userId",
        "userFullName",
        "userEmail",
        "position",
        "active"
})
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
