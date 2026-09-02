package com.realestate.backend.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.backend.common.response.ErrorResponse;
import com.realestate.backend.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        HttpServletRequest effectiveRequest = wrapIfHasBody(request);

        RateLimitResult result =
                rateLimitService.checkLimit(effectiveRequest);

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(result.remainingTokens())
        );

        if (result.allowed()) {
            filterChain.doFilter(effectiveRequest, response);
            return;
        }

        response.setHeader(
                "Retry-After",
                String.valueOf(result.retryAfterSeconds())
        );

        writeTooManyRequests(
                effectiveRequest,
                response,
                result.retryAfterSeconds()
        );
    }

    private HttpServletRequest wrapIfHasBody(
            HttpServletRequest request
    ) throws IOException {

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            return new CachedBodyHttpServletRequest(request);
        }

        return request;
    }

    private void writeTooManyRequests(
            HttpServletRequest request,
            HttpServletResponse response,
            long waitSeconds
    ) throws IOException {

        log.warn(
                "rate_limit_exceeded",
                kv("retryAfterSeconds", waitSeconds)
        );

        response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        ErrorResponse errorResponse = ErrorResponse.of(
                "Too many requests. Please try again in "
                        + waitSeconds
                        + " seconds.",
                HttpStatus.TOO_MANY_REQUESTS.value(),
                request.getRequestURI()
        );

        response.getWriter().write(
                objectMapper.writeValueAsString(errorResponse)
        );
    }
}