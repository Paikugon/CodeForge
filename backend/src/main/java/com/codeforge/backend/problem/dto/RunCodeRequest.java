package com.codeforge.backend.problem.dto;

import jakarta.validation.constraints.NotBlank;

public record RunCodeRequest(
        @NotBlank String language,
        @NotBlank String sourceCode,
        String input
) {}