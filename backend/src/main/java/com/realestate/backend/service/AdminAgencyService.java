package com.realestate.backend.service;

import com.realestate.backend.dto.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.response.AdminAgencyResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.enums.AgencyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminAgencyService {

    Page<AdminAgencyResponse> getAllAgencies(
            AdminAgencyFilterRequest filter,
            Pageable pageable
    );

    AdminAgencyResponse getAgencyById(UUID id);

    String changeAgencyStatus(UUID id, AgencyStatus status);

    String softDeleteAgency(UUID id);

    AgencySubscriptionResponse createAgencySubscription(UUID agencyId, UUID subscriptionId);

    AgencySubscriptionResponse getAgencySubscription(UUID agencyId);

}
