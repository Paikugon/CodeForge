package com.codeforge.backend.problem.dto;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.enums.ProblemDifficulty;
import com.codeforge.backend.submission.dto.ProblemSubmissionStats;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProblemListResponse(
        Long id,
        String title,
        String slug,
        Integer timeLimitMs,
        Integer memoryLimitMb,
        ProblemDifficulty difficulty,
        boolean enabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        long acceptedSubmissionCount,
        long judgedSubmissionCount,
        BigDecimal acceptanceRate) {

    public ProblemListResponse(
            Long id,
            String title,
            String slug,
            Integer timeLimitMs,
            Integer memoryLimitMb,
            ProblemDifficulty difficulty,
            boolean enabled,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        this(id, title, slug, timeLimitMs, memoryLimitMb, difficulty, enabled,
                createdAt, updatedAt, 0, 0, null);
    }

    public ProblemListResponse withSubmissionStats(ProblemSubmissionStats stats) {
        if (stats == null) {
            return this;
        }

        return new ProblemListResponse(
                id, title, slug, timeLimitMs, memoryLimitMb, difficulty, enabled,
                createdAt, updatedAt, stats.acceptedSubmissionCount(),
                stats.judgedSubmissionCount(), stats.acceptanceRate());
    }

    public static ProblemListResponse from(Problem problem) {
        return new ProblemListResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                problem.getDifficulty(),
                problem.isEnabled(),
                problem.getCreatedAt(),
                problem.getUpdatedAt());
    }
}
