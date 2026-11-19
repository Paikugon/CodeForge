package com.codeforge.backend.problem.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record ProblemBatchVisibilityRequest(@NotNull @Size(min = 1, max = 100) List<@NotNull @Positive Long> ids,
        @NotNull Boolean enabled) {}
