package com.realestate.backend.service.agency;

import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.security.CustomUserDetails;

public interface AgencyService {

    AgencyResponse getCurrentAgency(
            CustomUserDetails currentUser
    );

}
