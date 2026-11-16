package com.codeforge.backend.problem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RunCodeRequest(
        @NotBlank
        String language,

        @NotBlank
        @Size(max = 100_000)
        String sourceCode,

        @Size(max = 1_000_000)
        String input
) {
}