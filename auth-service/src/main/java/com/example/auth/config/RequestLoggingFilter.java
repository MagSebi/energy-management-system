package com.example.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        log.debug("INCOMING {} {} authHeaderPresent={} pathInfo={} query={}", request.getMethod(), request.getRequestURI(), authHeader != null, request.getPathInfo(), request.getQueryString());
        filterChain.doFilter(request, response);
        log.debug("OUTGOING {} {} status={}", request.getMethod(), request.getRequestURI(), response.getStatus());
    }
}

