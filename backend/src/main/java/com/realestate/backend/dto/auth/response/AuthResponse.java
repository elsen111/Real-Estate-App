package com.realestate.backend.dto.auth.response;

import com.realestate.backend.dto.agency.response.AgencyResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresInSeconds;
    private UserResponse user;
    private AgencyResponse agency;

}
