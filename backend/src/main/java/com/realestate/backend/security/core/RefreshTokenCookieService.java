package com.realestate.backend.security.core;

import com.realestate.backend.config.JwtConfig;
import com.realestate.backend.config.properties.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    public static final String COOKIE_NAME = "refresh_token";
    public static final String HEADER_NAME = "X-Refresh-Token";

    private final JwtConfig jwtConfig;
    private final CookieProperties cookieProperties;

    public ResponseCookie create(String rawToken) {
        return build(rawToken, jwtConfig.refreshTokenExpiration());
    }

    public ResponseCookie clear() {
        return build("", Duration.ZERO);
    }

    /**
     * Resolves the refresh token from the request.
     * The X-Refresh-Token header wins when present (explicit client intent,
     * and the only option for Swagger UI), otherwise the cookie is used.
     * Returns null when neither is present.
     */
    public String resolve(HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(header)) {
            return header.trim();
        }

        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            return cookie.getValue().trim();
        }

        return null;
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.path())
                .maxAge(maxAge)
                .build();
    }
}