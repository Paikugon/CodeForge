package com.codeforge.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TestCaseRequest(
    @NotNull String input,
    @NotNull String expectedOutput,
    Boolean isSample,
    @Min(0) Integer orderIndex
) {}