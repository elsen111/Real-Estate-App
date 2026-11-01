package com.realestate.backend.security.core;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.config.properties.CookieProperties;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthSessionResponder {

    private final RefreshTokenCookieService cookieService;
    private final CookieProperties cookieProperties;

    public ResponseEntity<ApiResponse<AuthResponse>> authenticated(
            HttpStatus status,
            String message,
            AuthResponse auth
    ) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        AuthResponse body = auth;

        if (auth.getRefreshToken() != null) {
            builder.header(
                    HttpHeaders.SET_COOKIE,
                    cookieService.create(auth.getRefreshToken()).toString()
            );

            if (!cookieProperties.exposeTokenInBody()) {
                body = auth.toBuilder().refreshToken(null).build();
            }
        }

        return builder.body(ApiResponse.success(message, body));
    }

    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshed(
            String message,
            RefreshTokenResponse tokens
    ) {
        RefreshTokenResponse body = cookieProperties.exposeTokenInBody()
                ? tokens
                : tokens.toBuilder().refreshToken(null).build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookieService.create(tokens.getRefreshToken()).toString()
                )
                .body(ApiResponse.success(message, body));
    }

    public ResponseEntity<ApiResponse<Void>> loggedOut(String message) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.clear().toString())
                .body(ApiResponse.success(message));
    }
}