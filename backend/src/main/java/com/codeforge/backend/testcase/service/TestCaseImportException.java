package com.codeforge.backend.testcase.service;

public class TestCaseImportException extends RuntimeException {
    private final String file;
    public TestCaseImportException(String code, String file) { super(code); this.file = file; }
    public String getFile() { return file; }
}
