package com.codeforge.backend.judge.dto;

public record RunCodeResult(
        Integer resultCode,
        String stdout,
        Integer exitCode,
        Integer executionTimeMs,
        Integer memoryUsedKb,
        String compileOutput
) {}