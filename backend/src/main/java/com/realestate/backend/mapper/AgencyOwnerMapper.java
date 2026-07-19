package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.AgencyOwnerResponse;
import com.realestate.backend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgencyOwnerMapper {

    @Mapping(target = "avatarUrl", source = "profilePhotoUrl.media.fileUrl")
    AgencyOwnerResponse toResponse(UserEntity owner);

}
