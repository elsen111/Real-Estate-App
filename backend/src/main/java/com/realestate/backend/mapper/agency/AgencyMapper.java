package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgencyMapper {

    AgencyResponse toAgencyOwnerResponse(AgencyEntity agency);

    @Mapping(target = "properties",  ignore = true)
    AdminAgencyResponse toAdminResponse(AgencyEntity agency);

    @Mapping(target = "status",  ignore = true)
    AgencyResponse toPublicAgencyListItem(AgencyEntity agency);

    @Mapping(target = "totalAgents", source = "totalAgents")
    @Mapping(target = "status",  ignore = true)
    AgencyResponse toPublicAgencyResponse(AgencyEntity agency, long totalAgents);

}
