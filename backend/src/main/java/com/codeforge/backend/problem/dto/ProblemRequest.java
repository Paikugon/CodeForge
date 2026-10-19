package com.codeforge.backend.problem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProblemRequest(
		@NotBlank @Size(max = 255) String title,

		@NotBlank @Size(max = 255) @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug,

		@NotBlank String description,

		@Positive Integer timeLimitMs,

		@Positive Integer memoryLimitMb) {
}