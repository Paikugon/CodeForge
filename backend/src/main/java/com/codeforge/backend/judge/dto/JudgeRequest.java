package com.codeforge.backend.judge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.JsonNode;

public record JudgeRequest(
		String src,

		@JsonProperty("language_config") Object languageConfig,

		@JsonProperty("max_cpu_time") int maxCpuTime,

		@JsonProperty("max_memory") long maxMemory,

		@JsonProperty("test_case_id") String testCaseId,

		boolean output,
		@JsonProperty("spj_version")
        String spjVersion,

        @JsonProperty("spj_config")
        JsonNode spjConfig,

        @JsonProperty("spj_compile_config")
        JsonNode spjCompileConfig,

        @JsonProperty("spj_src")
        String spjSrc){
}