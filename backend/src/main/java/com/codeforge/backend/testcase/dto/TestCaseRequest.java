package com.codeforge.backend.testcase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TestCaseRequest(
    @NotNull @Size(max = 1_000_000) String input,
    @NotNull @Size(max = 1_000_000) String expectedOutput,
    Boolean isSample,
    @Min(0) Integer orderIndex
) {}
