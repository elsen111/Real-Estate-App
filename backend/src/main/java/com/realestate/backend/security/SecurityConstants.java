package com.realestate.backend.security;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTH_HEADER = "Authorization";
    public static final String ROLE_PREFIX = "ROLE_";

    public static final String[] PUBLIC_URLS = {
            "/api/auth/register",
            "/api/auth/register/**",
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
}