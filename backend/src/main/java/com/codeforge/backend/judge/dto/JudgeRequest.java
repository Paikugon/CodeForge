package com.codeforge.backend.judge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JudgeRequest(
        String src,

        @JsonProperty("language_config")
        Object languageConfig,

        @JsonProperty("max_cpu_time")
        int maxCpuTime,

        @JsonProperty("max_memory")
        long maxMemory,

        @JsonProperty("test_case_id")
        String testCaseId,

        boolean output
) {}