package com.codeforge.backend.testcase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record TestCaseImportRequest(@NotNull @Size(min = 1, max = 100) List<@NotNull @Valid Entry> testCases) {
    public record Entry(@NotBlank @Size(max = 255) String name, Boolean isSample, @Min(0) Integer orderIndex) {}
}
