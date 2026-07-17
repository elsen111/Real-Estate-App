package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.agency.response.AgencyOwnerResponse;
import com.realestate.backend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface AgencyOwnerMapper {

    @Mapping(target = "avatarUrl", source = "profilePhotoUrl.media.fileUrl")
    AgencyOwnerResponse toResponse(UserEntity owner);
//    {
//        if(owner == null) return null;
//
//        return AgencyOwnerResponse.builder()
//                .id(owner.getId())
//                .fullName(owner.getFullName())
//                .email(owner.getEmail())
//                .phoneNumber(owner.getPhoneNumber())
//                .enabled(owner.getEnabled())
//                .emailVerified(owner.isEmailVerified())
//                .avatarUrl(owner.getProfilePhotoUrl())
//                .build();
//    }

}
