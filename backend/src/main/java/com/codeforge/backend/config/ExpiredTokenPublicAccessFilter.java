package com.codeforge.backend.config;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.web.filter.OncePerRequestFilter;

public class ExpiredTokenPublicAccessFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_STATUS = "X-Auth-Status";
    private static final String EXPIRED_STATUS = "expired";
    private static final String EXPIRED_DESCRIPTION_PREFIX = "Jwt expired at ";

    private final JwtDecoder jwtDecoder;
    private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    public ExpiredTokenPublicAccessFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token;
        boolean expired;

        try {
            token = bearerTokenResolver.resolve(request);
            expired = token != null && isExpired(token);
        } catch (OAuth2AuthenticationException | JwtException exception) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!expired) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.clearContext();
        response.setHeader(AUTH_STATUS, EXPIRED_STATUS);

        filterChain.doFilter(
                new WithoutAuthorizationHeaderRequest(request),
                response);
    }

    private boolean isExpired(String token) {
        try {
            jwtDecoder.decode(token);
            return false;
        } catch (JwtValidationException exception) {
            Collection<OAuth2Error> errors = exception.getErrors();

            return !errors.isEmpty()
                    && errors.stream().allMatch(error -> {
                        String description = error.getDescription();

                        return description != null
                                && description.startsWith(
                                        EXPIRED_DESCRIPTION_PREFIX);
                    });
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if ("GET".equals(method)) {
            return path.equals("/api/problems")
                    || path.equals("/api/problems/")
                    || path.matches("/api/problems/\\d+")
                    || path.matches("/api/problems/\\d+/(solved|unsolved)")
                    || path.equals("/api/tags")
                    || path.equals("/api/tags/")
                    || path.matches("/api/tags/\\d+")
                    || path.equals("/api/submissions")
                    || path.matches("/api/submissions/\\d+")
                    || path.matches("/api/users/profile/\\d+");
        }

        return "POST".equals(method) && path.matches("/api/problems/\\d+/run");
    }

    private static final class WithoutAuthorizationHeaderRequest
            extends HttpServletRequestWrapper {

        private WithoutAuthorizationHeaderRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            return AUTHORIZATION.equalsIgnoreCase(name)
                    ? null
                    : super.getHeader(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            return AUTHORIZATION.equalsIgnoreCase(name)
                    ? java.util.Collections.emptyEnumeration()
                    : super.getHeaders(name);
        }
    }
}
