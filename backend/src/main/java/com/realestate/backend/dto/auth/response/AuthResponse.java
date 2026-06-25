package com.realestate.backend.dto.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.dto.agency.response.AgencyMemberResponse;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({ "tokenType", "accessToken", "refreshToken", "expiresInSeconds"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long expiresInSeconds;
    private UserResponse user;
    private AgencyResponse agency;

}
