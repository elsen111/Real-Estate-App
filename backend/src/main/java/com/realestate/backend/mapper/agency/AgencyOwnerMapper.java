package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.agency.response.AgencyOwnerResponse;
import com.realestate.backend.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AgencyOwnerMapper {

    public AgencyOwnerResponse toResponse(UserEntity owner) {
        if(owner == null) return null;

        return AgencyOwnerResponse.builder()
                .id(owner.getId())
                .fullName(owner.getFullName())
                .email(owner.getEmail())
                .phoneNumber(owner.getPhoneNumber())
                .enabled(owner.getEnabled())
                .emailVerified(owner.isEmailVerified())
                .build();
    }

}
