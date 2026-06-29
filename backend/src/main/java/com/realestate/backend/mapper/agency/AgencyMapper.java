package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.admin.user.response.AdminUserResponse;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
public class AgencyMapper {

    public AgencyResponse toSummary(AgencyEntity agency) {
        if(agency == null) {
            return null;
        }

        return AgencyResponse.builder()
                .id(agency.getId())
                .name(agency.getName())
                .email(agency.getEmail())
                .phoneNumber(agency.getPhoneNumber())
                .city(agency.getCity())
                .address(agency.getAddress())
                .status(agency.getStatus())
                .build();
    }

    public AdminAgencyResponse toAdminResponse(AgencyEntity agency) {
        if(agency == null) {
            return null;
        }

        return AdminAgencyResponse.builder()
                .id(agency.getId())
                .name(agency.getName())
                .description(agency.getDescription())
                .phoneNumber(agency.getPhoneNumber())
                .email(agency.getEmail())
                .website(agency.getWebsite())
                .logoUrl(agency.getLogoUrl())
                .city(agency.getCity())
                .address(agency.getAddress())
                .status(agency.getStatus())
                .createdAt(agency.getCreatedAt())
                .updatedAt(agency.getUpdatedAt())
                .build();
    }

}
