
package com.codeforge.backend.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(
    @NotNull
    Long problemId,

    @NotBlank
    String language,

    @NotBlank
    @Size(max = 100_000)
    String sourceCode
) {}
