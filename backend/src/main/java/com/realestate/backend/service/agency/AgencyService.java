package com.realestate.backend.service.agency;

import com.realestate.backend.dto.agency.request.AgencyAgentFilterRequest;
import com.realestate.backend.dto.agency.response.AgencyLogoUploadResponse;
import com.realestate.backend.dto.property.request.PropertyFilterRequest;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.dto.agency.request.AgencyFilterRequest;
import com.realestate.backend.dto.agency.request.AgencyPropertyFilterRequest;
import com.realestate.backend.dto.agency.request.UpdateAgencyRequest;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.dto.user.response.UserProfilePhotoResponse;
import com.realestate.backend.dto.user.response.UserResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AgencyService {

    AgencyResponse getCurrentAgency(
            CustomUserDetails currentUser
    );

    AgencyResponse updateOwnAgency(
            CustomUserDetails currentUser,
            UpdateAgencyRequest request
    );

    AgencySubscriptionResponse getMySubscription(CustomUserDetails currentUser);

    Page<PropertyResponse> getMyAgencyProperties(
            CustomUserDetails currentUser,
            AgencyPropertyFilterRequest filter,
            Pageable pageable
    );

    Page<AgencyResponse> getAllPublicAgencies(
            AgencyFilterRequest filter,
            Pageable pageable
    );

    AgencyResponse getPublicAgencyInfo(UUID agencyId);

    Page<PropertyResponse> getAgencyProperties(
            UUID  agencyId,
            PropertyFilterRequest filter,
            Pageable pageable
    );

    Page<UserResponse> getAgencyAgents(
            UUID  agencyId,
            AgencyAgentFilterRequest filterRequest,
            Pageable pageable
    );

    AgencyLogoUploadResponse uploadLogo(
            MultipartFile file,
            CustomUserDetails currentUser
    );

}
