package com.realestate.backend.mapper.auth;

import com.realestate.backend.dto.auth.request.AgencyOwnerRegisterRequest;
import com.realestate.backend.dto.auth.request.UserRegisterRequest;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.AgencyStatus;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class AuthMapper {

    public UserEntity toUserEntity(UserRegisterRequest request) {

        return UserEntity.builder()
                .fullName(normalizeValue(request.getFullName()))
                .email(normalizeValue(request.getEmail()))
                .phoneNumber(normalizeValue(request.getPhoneNumber()))
                .enabled(true)
                .emailVerified(false)
                .roles(new HashSet<>())
                .build();
    }

    public UserEntity toAgencyOwnerUser(AgencyOwnerRegisterRequest request) {

        return UserEntity.builder()
                .fullName(normalizeValue(request.getOwnerFullName()))
                .email(normalizeValue(request.getOwnerEmail()))
                .phoneNumber(normalizeValue(request.getOwnerPhoneNumber()))
                .enabled(true)
                .emailVerified(false)
                .roles(new HashSet<>())
                .build();
    }

    public AgencyEntity toAgencyEntity(AgencyOwnerRegisterRequest request) {

        return AgencyEntity.builder()
                .name(normalizeValue(request.getAgencyName()))
                .description(normalizeValue(request.getAgencyDescription()))
                .phoneNumber(normalizeValue(request.getAgencyPhoneNumber()))
                .email(normalizeValue(request.getAgencyEmail()))
                .website(normalizeValue(request.getAgencyWebsite()))
                .city(normalizeValue(request.getCity()))
                .address(normalizeValue(request.getAddress()))
                .status(AgencyStatus.PENDING)
                .build();
    }

    private String normalizeValue(String value) {
        if(value == null)
            return null;

        if(value.contains("@")) {
            value = value.toLowerCase();
        }

        return value.trim();
    }

}
