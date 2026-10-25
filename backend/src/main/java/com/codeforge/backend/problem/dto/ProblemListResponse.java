package com.codeforge.backend.problem.dto;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.enums.ProblemDifficulty;

import java.time.OffsetDateTime;

public record ProblemListResponse(
        Long id,
        String title,
        String slug,
        Integer timeLimitMs,
        Integer memoryLimitMb,
        ProblemDifficulty difficulty,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static ProblemListResponse from(Problem problem) {
        return new ProblemListResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                problem.getDifficulty(),
                problem.getCreatedAt(),
                problem.getUpdatedAt());
    }
}
