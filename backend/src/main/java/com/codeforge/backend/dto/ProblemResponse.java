package com.codeforge.backend.dto;

import com.codeforge.backend.entity.Problem;

import java.time.OffsetDateTime;

public record ProblemResponse(
    Long id,
    String title,
    String slug,
    String description,
    Integer timeLimitMs,
    Integer memoryLimitMb,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

    public static ProblemResponse from(Problem problem) {
        return new ProblemResponse(
            problem.getId(),
            problem.getTitle(),
            problem.getSlug(),
            problem.getDescription(),
            problem.getTimeLimitMs(),
            problem.getMemoryLimitMb(),
            problem.getCreatedAt(),
            problem.getUpdatedAt()
        );
    }
}
