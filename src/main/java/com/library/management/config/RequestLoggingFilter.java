package com.library.management.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!shouldSkip(request.getRequestURI())) {
                long durationMs = System.currentTimeMillis() - start;
                log.info(
                        "{} {} -> {} ({} ms)",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        durationMs
                );
            }
        }
    }

    private boolean shouldSkip(String uri) {
        return uri.startsWith("/actuator")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs");
    }
}
