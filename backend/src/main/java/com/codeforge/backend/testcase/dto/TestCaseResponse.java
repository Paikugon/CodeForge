package com.codeforge.backend.testcase.dto;

import java.time.OffsetDateTime;

public record TestCaseResponse(
    Long id,
    Long problemId,
    String input,
    String expectedOutput,
    Boolean isSample,
    Integer orderIndex,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}