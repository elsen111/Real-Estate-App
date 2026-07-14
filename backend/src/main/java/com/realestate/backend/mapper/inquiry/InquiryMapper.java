package com.realestate.backend.mapper.inquiry;

import com.realestate.backend.dto.inquiry.response.InquiryClientResponse;
import com.realestate.backend.entity.InquiryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InquiryMapper {

    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "propertyTitle", source = "property.title")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", source = "client.fullName")
    @Mapping(target = "clientPhone", source = "client.phoneNumber")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "assignedAgentId", source = "assignedAgent.id")
    @Mapping(target = "agencyId", source = "agency.id")
    InquiryClientResponse toClientResponse(InquiryEntity inquiry);

}
