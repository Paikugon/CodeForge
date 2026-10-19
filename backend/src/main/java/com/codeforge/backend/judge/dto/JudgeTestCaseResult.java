package com.codeforge.backend.judge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JudgeTestCaseResult(
		@JsonProperty("cpu_time") long cpuTime,

		@JsonProperty("real_time") long realTime,

		long memory,
		int signal,

		@JsonProperty("exit_code") int exitCode,

		int error,
		int result,

		@JsonProperty("test_case") String testCase,

		@JsonProperty("output_md5") String outputMd5,

		String output) {
}