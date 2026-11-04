package com.codeforge.backend.problem.dto;

import com.codeforge.backend.problem.enums.ProblemDifficulty;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.testcase.dto.TestCaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

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
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<TestCaseResponse> testCases
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
            problem.getCreatedAt(),
            problem.getUpdatedAt(),
            testCases
        );
    }
}
