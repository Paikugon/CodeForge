package com.codeforge.backend.problem.dto;

public record RunCodeResponse(
		Integer resultCode,
		String stdout,
		Integer exitCode,
		Integer executionTimeMs,
		Integer memoryUsedKb,
		String compileOutput) {
}