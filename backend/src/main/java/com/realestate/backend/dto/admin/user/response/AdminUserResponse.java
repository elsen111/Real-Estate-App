package com.realestate.backend.dto.admin.user.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUserResponse {

    UUID id;
    String fullName;
    String email;
    String phoneNumber;
    Set<String> roles;
    Boolean enabled;
    Boolean emailVerified;
    String position;
    String agency;
    LocalDateTime createdAt;

}
