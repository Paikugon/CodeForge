package com.codeforge.backend.submission.dto;

import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import com.codeforge.backend.submission.model.Submission;

import java.time.OffsetDateTime;

public record SubmissionListResponse(
        Long id,
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

    public static SubmissionListResponse from(Submission submission) {
        return new SubmissionListResponse(
                submission.getId(),
                submission.getProblemId(),
                submission.getLanguage(),
                submission.getStatus(),
                submission.getVerdict(),
                submission.getExecutionTimeMs(),
                submission.getMemoryUsedKb(),
                submission.getPassedTestCases(),
                submission.getTotalTestCases(),
                submission.getCreatedAt(),
                submission.getUpdatedAt());
    }
}
