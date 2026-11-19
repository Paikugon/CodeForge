package com.codeforge.backend.testcase.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record TestCaseSelectionRequest(@NotNull @Size(min = 1, max = 100) List<@NotNull @Positive Long> ids) {}
