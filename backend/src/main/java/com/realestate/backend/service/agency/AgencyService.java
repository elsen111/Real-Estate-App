package com.realestate.backend.service.agency;

import com.realestate.backend.dto.agency.request.UpdateAgencyRequest;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.hibernate.sql.Update;

public interface AgencyService {

    AgencyResponse getCurrentAgency(
            CustomUserDetails currentUser
    );

    AgencyResponse updateOwnAgency(
            CustomUserDetails currentUser,
            UpdateAgencyRequest request
    );

    AgencySubscriptionResponse getMySubscription(CustomUserDetails currentUser);

}
