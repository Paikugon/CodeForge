package com.codeforge.backend.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    boolean mustChangePassword
) {}