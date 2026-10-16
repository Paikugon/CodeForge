package com.codeforge.backend.judge.dto;

import com.codeforge.backend.submission.enums.SubmissionVerdict;

public record JudgeResult(
        SubmissionVerdict verdict,
        Integer executionTimeMs,
        Integer memoryUsedKb,
        Integer passedTestCases,
        Integer totalTestCases,
        String compileOutput,
        String failedTestCase,
        String failedOutput
) {
}