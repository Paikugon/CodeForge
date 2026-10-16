package com.codeforge.backend.submission.dto;

public record FailedTestCaseResponse(
        String testCase,
        String input,
        String expectedOutput,
        String actualOutput
) {}
