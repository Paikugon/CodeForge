package com.codeforge.backend.testcase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TestCaseBatchRequest(
        @NotNull @Size(min = 1, max = 100)
        List<@NotNull @Valid TestCaseRequest> testCases
) {}
