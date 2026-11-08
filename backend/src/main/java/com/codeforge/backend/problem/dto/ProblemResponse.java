package com.codeforge.backend.problem.dto;

import com.codeforge.backend.problem.enums.ProblemDifficulty;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.submission.dto.ProblemSubmissionStats;
import com.codeforge.backend.testcase.dto.TestCaseResponse;
import com.codeforge.backend.tag.dto.TagResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ProblemResponse(
    Long id,
    String title,
    String slug,
    ProblemDifficulty difficulty,
    String description,
    Integer timeLimitMs,
    Integer memoryLimitMb,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean spjEnabled,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String spjSource,
    boolean enabled,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<TagResponse> tags,
    List<TestCaseResponse> testCases,
    long acceptedSubmissionCount,
    long judgedSubmissionCount,
    BigDecimal acceptanceRate
) {

    public static ProblemResponse from(Problem problem) {
        return from(problem, List.of(), false);
    }

    public static ProblemResponse from(
        Problem problem,
        List<TestCaseResponse> testCases
    ) {
        return from(problem, testCases, false);
    }

    public static ProblemResponse from(
        Problem problem,
        List<TestCaseResponse> testCases,
        boolean includeSpecialJudgeDetails
    ) {
        return new ProblemResponse(
            problem.getId(),
            problem.getTitle(),
            problem.getSlug(),
            problem.getDifficulty(),
            problem.getDescription(),
            problem.getTimeLimitMs(),
            problem.getMemoryLimitMb(),
            includeSpecialJudgeDetails ? problem.isSpjEnabled() : null,
            includeSpecialJudgeDetails ? problem.getSpjSource() : null,
            problem.isEnabled(),
            problem.getCreatedAt(),
            problem.getUpdatedAt(),
            problem.getTags().stream().map(TagResponse::from).toList(),
            testCases,
            0,
            0,
            null
        );
    }

    public ProblemResponse withSubmissionStats(ProblemSubmissionStats stats) {
        if (stats == null) {
            return this;
        }

        return new ProblemResponse(
            id, title, slug, difficulty, description, timeLimitMs, memoryLimitMb,
            spjEnabled, spjSource, enabled, createdAt, updatedAt, tags, testCases,
            stats.acceptedSubmissionCount(), stats.judgedSubmissionCount(),
            stats.acceptanceRate()
        );
    }

}
