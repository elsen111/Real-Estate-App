package com.realestate.backend.service.admin.agency;

import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.enums.AgencyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.swing.text.html.Option;
import java.util.Optional;
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

}
