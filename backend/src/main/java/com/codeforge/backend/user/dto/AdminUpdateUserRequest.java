package com.codeforge.backend.user.dto;

import com.codeforge.backend.user.enums.UserRole;

public record AdminUpdateUserRequest(
        UserRole role,
        Boolean enabled
) {}