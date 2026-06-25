package com.realestate.backend.dto.agency.response;

import com.realestate.backend.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
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
