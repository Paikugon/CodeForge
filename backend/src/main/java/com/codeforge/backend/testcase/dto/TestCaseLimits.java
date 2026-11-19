package com.codeforge.backend.testcase.dto;

public final class TestCaseLimits {
    public static final int MAX_FILE_BYTES = 5 * 1024 * 1024;
    public static final long MAX_BATCH_BYTES = 50L * 1024 * 1024;
    public static final int MAX_CASES = 100;
    private TestCaseLimits() {}
}
