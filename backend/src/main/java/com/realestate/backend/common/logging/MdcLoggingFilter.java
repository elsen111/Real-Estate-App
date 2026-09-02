package com.realestate.backend.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID = "correlationId";
    public static final String ACTOR_ID = "actorId";
    public static final String ACTOR_ROLE = "actorRole";
    public static final String PATH = "path";
    public static final String METHOD = "method";

    private static final List<String> ROLE_PRIORITY_LIST = List.of(
            "ROLE_SUPER_ADMIN",
            "ROLE_ADMIN",
            "ROLE_AGENCY_OWNER",
            "ROLE_AGENT",
            "ROLE_LANDLORD",
            "ROLE_CLIENT"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = UUID.randomUUID().toString();

        try {
            MDC.put(CORRELATION_ID, correlationId);
            MDC.put(PATH, request.getRequestURI());
            MDC.put(METHOD, request.getMethod());

            populateActor();

            response.setHeader("X-Correlation-ID", correlationId);

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }

    private void populateActor() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        MDC.put(ACTOR_ID, authentication.getName());

        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(ROLE_PRIORITY_LIST::contains)
                .min(Comparator.comparingInt(ROLE_PRIORITY_LIST::indexOf))
                .orElseThrow(() -> new IllegalStateException("User has no recognized roles matching the system priority list"));


        if (!role.isBlank()) {
            MDC.put(ACTOR_ROLE, role);
        }
    }
}