package com.codeforge.backend.problem.dto;

import com.codeforge.backend.problem.enums.ProblemDifficulty;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.testcase.dto.TestCaseResponse;

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
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<TestCaseResponse> testCases
) {

    public static ProblemResponse from(Problem problem) {
        return from(problem, List.of());
    }

    public static ProblemResponse from(
        Problem problem,
        List<TestCaseResponse> testCases
    ) {
        return new ProblemResponse(
            problem.getId(),
            problem.getTitle(),
            problem.getSlug(),
            problem.getDifficulty(),
            problem.getDescription(),
            problem.getTimeLimitMs(),
            problem.getMemoryLimitMb(),
            problem.getCreatedAt(),
            problem.getUpdatedAt(),
            testCases
        );
    }
}
