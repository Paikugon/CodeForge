package com.codeforge.backend.user.dto;

import java.time.OffsetDateTime;

import com.codeforge.backend.user.enums.UserRole;

public record UserListResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        boolean enabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}