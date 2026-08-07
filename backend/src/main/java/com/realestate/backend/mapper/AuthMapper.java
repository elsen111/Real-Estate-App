package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.AgencyOwnerRegisterRequest;
import com.realestate.backend.dto.request.UserRegisterRequest;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "fullName", source = "fullName", qualifiedByName = "normalize")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalize")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "normalize")
    @Mapping(target = "roles", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    UserEntity toUserEntity(UserRegisterRequest userRegisterRequest);

    @Mapping(target = "fullName", source = "fullName", qualifiedByName = "normalize")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalize")
    @Mapping(target = "phoneNumber", source = "businessPhone", qualifiedByName = "normalize")
    @Mapping(target = "roles", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    UserEntity toAgencyOwnerUser(AgencyOwnerRegisterRequest request);

    @Mapping(target = "name", source = "agencyName", qualifiedByName = "normalize")
    @Mapping(target = "description", source = "agencyDescription", qualifiedByName = "normalize")
    @Mapping(target = "phoneNumber", source = "businessPhone", qualifiedByName = "normalize")
    @Mapping(target = "website", source = "agencyWebsiteUrl", qualifiedByName = "normalize")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalize")
    @Mapping(target = "city", source = "city", qualifiedByName = "normalize")
    @Mapping(target = "address", source = "address", qualifiedByName = "normalize")
    @Mapping(target = "status", expression = "java(com.realestate.backend.enums.AgencyStatus.PENDING)")
    AgencyEntity toAgencyEntity(AgencyOwnerRegisterRequest request);

    @Named("normalize")
    default String normalizeValue(String value) {
        if(value == null)
            return null;

        if(value.contains("@")) {
            value = value.toLowerCase();
        }

        return value.trim();
    }

}
