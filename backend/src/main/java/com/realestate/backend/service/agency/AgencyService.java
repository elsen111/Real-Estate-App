package com.realestate.backend.service.agency;

import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.admin.property.request.AdminPropertyFilterRequest;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import com.realestate.backend.dto.agency.request.AgencyFilterRequest;
import com.realestate.backend.dto.agency.request.AgencyPropertyFilterRequest;
import com.realestate.backend.dto.agency.request.UpdateAgencyRequest;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AgencyService {

    AgencyResponse getCurrentAgency(
            CustomUserDetails currentUser
    );

    AgencyResponse updateOwnAgency(
            CustomUserDetails currentUser,
            UpdateAgencyRequest request
    );

    AgencySubscriptionResponse getMySubscription(CustomUserDetails currentUser);

    Page<AdminPropertyResponse> getMyAgencyProperties(
            CustomUserDetails currentUser,
            AgencyPropertyFilterRequest filter,
            Pageable pageable
    );

    Page<AgencyResponse> getAllPublicAgencies(
            AgencyFilterRequest filter,
            Pageable pageable
    );

}
