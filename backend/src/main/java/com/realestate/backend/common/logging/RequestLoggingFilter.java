package com.realestate.backend.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {

            long durationMs = System.currentTimeMillis() - start;

            int status = response.getStatus();

            if (status >= 500) {
                log.error(
                        "request_completed",
                        kv("status", status),
                        kv("durationMs", durationMs)
                );
            } else if (status >= 400) {
                log.warn(
                        "request_completed",
                        kv("status", status),
                        kv("durationMs", durationMs)
                );
            } else {
                log.info(
                        "request_completed",
                        kv("status", status),
                        kv("durationMs", durationMs)
                );
            }
        }
    }
}