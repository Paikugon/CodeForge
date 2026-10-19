package com.codeforge.backend.judge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record JudgeRunRequest(
        String src,

        @JsonProperty("language_config") Object languageConfig,

        @JsonProperty("max_cpu_time") int maxCpuTime,

        @JsonProperty("max_memory") long maxMemory,

        @JsonProperty("test_case") List<RunTestCase> testCase,

        boolean output) {
    public record RunTestCase(
            String input,
            String output) {
    }
}