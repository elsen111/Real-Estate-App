package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AgencyMapper {

    @Mapping(target = "logoUrl", source = "logoUrl.media.fileUrl")
    AgencyResponse toAgencyOwnerResponse(AgencyEntity agency);

    @Mapping(target = "properties",  ignore = true)
    @Mapping(target = "logoUrl", source = "logoUrl.media.fileUrl")
    AdminAgencyResponse toAdminResponse(AgencyEntity agency);

    @Mapping(target = "status",  ignore = true)
    @Mapping(target = "logoUrl", source = "logoUrl.media.fileUrl")
    AgencyResponse toPublicAgencyListItem(AgencyEntity agency);

    @Mapping(target = "totalAgents", source = "totalAgents")
    @Mapping(target = "logoUrl", source = "agency.logoUrl.media.fileUrl")
    @Mapping(target = "status",  ignore = true)
    AgencyResponse toPublicAgencyResponse(AgencyEntity agency, long totalAgents);

    @Named("propertyAgency")
    @Mapping(target = "logoUrl", source = "logoUrl.media.fileUrl")
    AgencyResponse toPropertyResponse(AgencyEntity agency);

}
