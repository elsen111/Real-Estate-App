package com.realestate.backend.service;

import com.realestate.backend.dto.response.AgencyMemberResponse;
import com.realestate.backend.enums.Role;
import com.realestate.backend.security.CustomUserDetails;

import java.util.UUID;

public interface AgencyMemberService {

    AgencyMemberResponse assignAgent(
            UUID agencyId,
            UUID userId,
            CustomUserDetails currentUser
    );

    AgencyMemberResponse assignMember(
            UUID agencyId,
            UUID userId,
            Role role,
            String defaultPosition
    );

}
