package com.codeforge.backend.user.dto;

import java.time.OffsetDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String role,
        long solvedCount,
        long acceptedSubmissionCount,
        long submissionCount,
        long unsolvedCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Boolean mustChangePassword) {
}
