package com.codeforge.backend.testcase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TestCaseRequest(
    @NotNull @Size(max = TestCaseLimits.MAX_FILE_BYTES) String input,
    @NotNull @Size(max = TestCaseLimits.MAX_FILE_BYTES) String expectedOutput,
    Boolean isSample,
    @Min(0) Integer orderIndex
) {}
