package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
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

}
