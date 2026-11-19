package com.codeforge.backend.testcase.dto;

import java.time.OffsetDateTime;

public record TestCaseSummary(Long id, Boolean isSample, Integer orderIndex,
        Integer inputCharacters, Integer outputCharacters, OffsetDateTime updatedAt) {}
