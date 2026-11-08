package com.codeforge.backend.problem.dto;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.enums.ProblemDifficulty;
import com.codeforge.backend.submission.dto.ProblemSubmissionStats;
import com.codeforge.backend.tag.dto.TagResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

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
        List<TagResponse> tags,
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
                createdAt, updatedAt, List.of(), 0, 0, null);
    }

    public ProblemListResponse(
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
        this(id, title, slug, timeLimitMs, memoryLimitMb, difficulty, enabled,
                createdAt, updatedAt, List.of(), acceptedSubmissionCount,
                judgedSubmissionCount, acceptanceRate);
    }

    public ProblemListResponse withTags(List<TagResponse> tags) {
        return new ProblemListResponse(
                id, title, slug, timeLimitMs, memoryLimitMb, difficulty, enabled,
                createdAt, updatedAt, tags, acceptedSubmissionCount,
                judgedSubmissionCount, acceptanceRate);
    }

    public ProblemListResponse withSubmissionStats(ProblemSubmissionStats stats) {
        if (stats == null) {
            return this;
        }

        return new ProblemListResponse(
                id, title, slug, timeLimitMs, memoryLimitMb, difficulty, enabled,
                createdAt, updatedAt, tags, stats.acceptedSubmissionCount(),
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
                problem.getUpdatedAt(),
                problem.getTags().stream().map(TagResponse::from).toList(),
                0,
                0,
                null);
    }
}
