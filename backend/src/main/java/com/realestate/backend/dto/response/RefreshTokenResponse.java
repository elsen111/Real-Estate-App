package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresInSeconds;

}
