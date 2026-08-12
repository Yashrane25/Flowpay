package com.yashrane.flowpay_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter  extends OncePerRequestFilter {
    private final RateLimiterService rateLimiterService;

    public RateLimitFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only rate-limit the transfer endpoint for this step — deliberately
        // scoped, rather than applying globally, since different endpoints
        // may warrant very different limits (a future enhancement, not
        // needed for this project's scope right now).
        if (!request.getRequestURI().equals("/api/transfer")) {
            filterChain.doFilter(request, response);
            return;
        }

        // We key on the AUTHENTICATED user's identity (set by JwtAuthFilter,
        // which MUST run before this filter — enforced by registration
        // order in SecurityConfig, see below). Anonymous/unauthenticated
        // requests to a protected endpoint are already rejected by Step
        // 12's authorization rule anyway, so we don't need a fallback
        // IP-based key here.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            filterChain.doFilter(request, response); // let it proceed; auth check downstream handles rejection
            return;
        }

        String userKey = auth.getName(); // the email set in JwtAuthFilter

        if (!rateLimiterService.tryConsume(userKey)) {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Rate limit exceeded. Please slow down.\"}");
            return; // deliberately DO NOT call filterChain.doFilter — request stops here
        }

        filterChain.doFilter(request, response);
    }
}
