package com.codeforge.backend.auth.dto;

import com.codeforge.backend.user.enums.UserRole;

public record UserResponse(
        Long id,
        String username,
        String email,
        UserRole role) {
}