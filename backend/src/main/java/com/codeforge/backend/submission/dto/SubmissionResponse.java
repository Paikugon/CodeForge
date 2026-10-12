
package com.codeforge.backend.submission.dto;

import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.enums.SubmissionVerdict;

import java.time.OffsetDateTime;

public record SubmissionResponse(
    Long id,
    Long problemId,
    String language,
    String sourceCode,

    SubmissionStatus status,
    SubmissionVerdict verdict,

    Integer executionTimeMs,
    Integer memoryUsedKb,

    Integer passedTestCases,
    Integer totalTestCases,

    String compileOutput,
    String errorMessage,

    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
