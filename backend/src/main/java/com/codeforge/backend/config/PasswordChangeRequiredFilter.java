package com.codeforge.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        boolean required = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "PASSWORD_CHANGE_REQUIRED".equals(
                                authority.getAuthority()));

        String path = request.getServletPath();
        String method = request.getMethod();

        boolean allowed = ("GET".equals(method)
                && "/api/users/profile/me".equals(path))
                || ("PUT".equals(method)
                        && "/api/users/me/password".equals(path));

        if (required && !allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {
                      "code": "PASSWORD_CHANGE_REQUIRED",
                      "message": "Please change your password before continuing."
                    }
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}