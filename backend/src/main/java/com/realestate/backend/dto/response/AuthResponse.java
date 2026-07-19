package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    private AuthUserResponse user;
    private AgencyResponse agency;

}
