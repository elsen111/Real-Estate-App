package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.admin.user.response.AdminUserResponse;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import lombok.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface AgencyMapper {

    AgencyResponse toAgencyOwnerResponse(AgencyEntity agency);

    @Mapping(target = "properties",  ignore = true)
    AdminAgencyResponse toAdminResponse(AgencyEntity agency);

    @Mapping(target = "status",  ignore = true)
    AgencyResponse toPublicAgencyResponse(AgencyEntity agency);

}
