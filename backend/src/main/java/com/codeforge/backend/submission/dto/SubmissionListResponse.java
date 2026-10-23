package com.codeforge.backend.submission.dto;

import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.enums.SubmissionVerdict;

import java.time.OffsetDateTime;

public record SubmissionListResponse(
        Long id,
        Long userId,
        String username,
        Long problemId,
        String language,
        SubmissionStatus status,
        SubmissionVerdict verdict,
        Integer executionTimeMs,
        Integer memoryUsedKb,
        Integer passedTestCases,
        Integer totalTestCases,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

}
