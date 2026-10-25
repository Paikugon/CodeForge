
package com.codeforge.backend.submission.dto;

import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

public record SubmissionResponse(
        Long id,
        Long userId,
        String username,
        Long problemId,
        String language,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String sourceCode,

        SubmissionStatus status,
        SubmissionVerdict verdict,

        Integer executionTimeMs,
        Integer memoryUsedKb,

        Integer passedTestCases,
        Integer totalTestCases,

        String compileOutput,
        String errorMessage,

        FailedTestCaseResponse failedTestCase,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
