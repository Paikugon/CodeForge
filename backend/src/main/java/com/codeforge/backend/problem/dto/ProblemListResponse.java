package com.codeforge.backend.problem.dto;

import com.codeforge.backend.problem.model.Problem;

import java.time.OffsetDateTime;

public record ProblemListResponse(
        Long id,
        String title,
        String slug,
        Integer timeLimitMs,
        Integer memoryLimitMb,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static ProblemListResponse from(Problem problem) {
        return new ProblemListResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                problem.getCreatedAt(),
                problem.getUpdatedAt());
    }
}
