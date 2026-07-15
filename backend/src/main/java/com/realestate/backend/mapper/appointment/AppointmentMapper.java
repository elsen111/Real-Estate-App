package com.realestate.backend.mapper.appointment;

import com.realestate.backend.dto.appointment.response.AppointmentResponse;
import com.realestate.backend.dto.inquiry.response.InquiryResponse;
import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.entity.InquiryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "propertyTitle", source = "property.title")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", source = "client.fullName")
    @Mapping(target = "clientPhone", source = "client.phoneNumber")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "agentId", source = "agent.id")
    @Mapping(target = "agencyId", source = "agency.id")
    AppointmentResponse toResponse(AppointmentEntity appointment);

}
