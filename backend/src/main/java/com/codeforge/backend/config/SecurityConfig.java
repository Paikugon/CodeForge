package com.codeforge.backend.config;

import com.codeforge.backend.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            UserRepository repository) {

        return email -> repository.findByEmail(email)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())
                        .disabled(!user.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        var provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(
            UserRepository repository) {

        var converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            long userId;

            try {
                userId = Long.parseLong(jwt.getSubject());
            } catch (NumberFormatException ex) {
                throw new InvalidBearerTokenException("Invalid token subject");
            }

            var user = repository.findById(userId)
                    .orElseThrow(() -> new InvalidBearerTokenException("User not found"));

            if (!user.isEnabled()) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                "ACCOUNT_DISABLED",
                                "Tài khoản của bạn đã bị khóa",
                                null));
            }

            Number tokenVersion = jwt.getClaim("token_version");

            if (tokenVersion == null
                    || tokenVersion.longValue() != user.getTokenVersion()) {
                throw new InvalidBearerTokenException(
                        "Token has been revoked. Please log in again");
            }

            if (user.isMustChangePassword()) {
                return java.util.List.of(
                        new SimpleGrantedAuthority("PASSWORD_CHANGE_REQUIRED"));
            }

            return java.util.List.of(
                    new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name()));
        });

        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/profile/me")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me/password")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/problems/me/solved")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/problems/me/unsolved")
                        .authenticated()
                        .requestMatchers(
                                "/api/problems/*/test-cases",
                                "/api/problems/*/test-cases/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/problems/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/problems/*/run")
                        .permitAll()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/submissions/me")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/submissions/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/profile/*")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationEntryPoint((request, response, exception) -> {
                            boolean disabled = exception instanceof OAuth2AuthenticationException oauthEx
                                    && "ACCOUNT_DISABLED".equals(oauthEx.getError().getErrorCode());

                            response.setStatus(disabled ? 403 : 401);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            response.getWriter().write(disabled
                                    ? """
                                            {
                                              "code": "ACCOUNT_DISABLED",
                                              "message": "Tài khoản của bạn đã bị khóa"
                                            }
                                            """
                                    : """
                                            {
                                              "code": "UNAUTHORIZED",
                                              "message": "Phiên đăng nhập không hợp lệ hoặc đã hết hạn"
                                            }
                                            """);
                        })
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                        .addFilterAfter(new PasswordChangeRequiredFilter(), BearerTokenAuthenticationFilter.class)
                .build();
    }
}