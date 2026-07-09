package com.realestate.backend.mapper.property;

import com.realestate.backend.dto.admin.property.response.AdminAgencyPropertyResponse;
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

}
