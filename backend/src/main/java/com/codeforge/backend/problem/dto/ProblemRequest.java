package com.codeforge.backend.problem.dto;

import com.codeforge.backend.problem.enums.ProblemDifficulty;
import com.codeforge.backend.testcase.dto.TestCaseRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.List;

public record ProblemRequest(
		@NotBlank @Size(max = 255) String title,

		@NotBlank @Size(max = 255) @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug,

		@NotBlank @Size(max = 100_000) String description,

		ProblemDifficulty difficulty,

		@Positive @Max(30_000) Integer timeLimitMs,

		@Positive @Max(1024) Integer memoryLimitMb,

		Boolean spjEnabled,
		@Size(max = 65_536) String spjSource,
		
		Boolean enabled,

		@Size(max = 20) Set<@NotNull @Positive Long> tagIds,

		@Size(max = 100) List<@NotNull @Valid TestCaseRequest> testCases
	) {

	public ProblemRequest {
		if (title != null) title = title.strip();
		if (slug != null) slug = slug.strip();
	}

	@AssertTrue(message = "CODE_SPJ_SOURCE_REQUIRED")
	public boolean isSpjSourceValid() {
		return !Boolean.TRUE.equals(spjEnabled)
				|| (spjSource != null && !spjSource.isBlank());
	}

}
