package com.realestate.backend.mapper.property;

import com.realestate.backend.dto.admin.property.response.AdminAgencyPropertyResponse;
import com.realestate.backend.dto.property.request.CreatePropertyRequest;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.entity.PropertyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    AdminAgencyPropertyResponse toAdminResponse(PropertyEntity property);

    PropertyResponse toAdminPropertyResponse(PropertyEntity property);

    @Mapping(target = "status", ignore = true)
    PropertyResponse toPublicAgencyPropertyResponse(PropertyEntity property);

    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "agencyName", source = "agency.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "assignedAgentId", source = "assignedAgent.id")
    @Mapping(target = "assignedAgentName", source = "assignedAgent.fullName")
    PropertyResponse toDetailResponse(PropertyEntity property);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "assignedAgent", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PropertyEntity toEntity(CreatePropertyRequest request);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "agencyName", source = "agency.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "assignedAgentId", source = "assignedAgent.id")
    @Mapping(target = "assignedAgentName", source = "assignedAgent.fullName")
    PropertyResponse toPublicClientResponse(PropertyEntity property);

}
