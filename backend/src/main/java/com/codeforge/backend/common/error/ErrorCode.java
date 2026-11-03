package com.codeforge.backend.common.error;

public final class ErrorCode {

    public static final String ACCOUNT_BANNED = "CODE_ACCOUNT_BANNED";
    public static final String ACCOUNT_DISABLED = "CODE_ACCOUNT_DISABLED";
    public static final String AUTHENTICATION_REQUIRED = "CODE_AUTHENTICATION_REQUIRED";
    public static final String FORBIDDEN = "CODE_FORBIDDEN";
    public static final String CURRENT_PASSWORD_INCORRECT = "CODE_CURRENT_PASSWORD_INCORRECT";
    public static final String EMAIL_ALREADY_EXISTS = "CODE_EMAIL_ALREADY_EXISTS";
    public static final String INVALID_EMAIL_OR_PASSWORD = "CODE_INVALID_EMAIL_OR_PASSWORD";
    public static final String INVALID_REQUEST_BODY = "CODE_INVALID_REQUEST_BODY";
    public static final String INVALID_SORT_PROPERTY = "CODE_INVALID_SORT_PROPERTY";
    public static final String INVALID_TOKEN = "CODE_INVALID_TOKEN";
    public static final String INVALID_VALUE = "CODE_INVALID_VALUE";
    public static final String LAST_ACTIVE_ADMIN = "CODE_LAST_ACTIVE_ADMIN";
    public static final String NEW_PASSWORD_MUST_DIFFER = "CODE_NEW_PASSWORD_MUST_DIFFER";
    public static final String PASSWORD_BCRYPT_LIMIT = "CODE_PASSWORD_BCRYPT_LIMIT";
    public static final String PASSWORD_CHANGE_REQUIRED = "CODE_PASSWORD_CHANGE_REQUIRED";
    public static final String PASSWORD_LENGTH_INVALID = "CODE_PASSWORD_LENGTH_INVALID";
    public static final String PROBLEM_ALREADY_EXISTS = "CODE_PROBLEM_ALREADY_EXISTS";
    public static final String PROBLEM_HAS_NO_TEST_CASES = "CODE_PROBLEM_HAS_NO_TEST_CASES";
    public static final String PROBLEM_NOT_FOUND = "CODE_PROBLEM_NOT_FOUND";
    public static final String REQUEST_VALIDATION_FAILED = "CODE_REQUEST_VALIDATION_FAILED";
    public static final String SUBMISSION_NOT_FOUND = "CODE_SUBMISSION_NOT_FOUND";
    public static final String TEST_CASE_NOT_FOUND = "CODE_TEST_CASE_NOT_FOUND";
    public static final String UNEXPECTED_ERROR = "CODE_UNEXPECTED_ERROR";
    public static final String UNSUPPORTED_LANGUAGE = "CODE_UNSUPPORTED_LANGUAGE";
    public static final String USER_NOT_FOUND = "CODE_USER_NOT_FOUND";
    public static final String USERNAME_ALREADY_EXISTS = "CODE_USERNAME_ALREADY_EXISTS";
    public static final String USERNAME_OR_EMAIL_ALREADY_EXISTS = "CODE_USERNAME_OR_EMAIL_ALREADY_EXISTS";
    public static final String UNKNOWN_ERROR = "CODE_UNKNOWN_ERROR";

    private ErrorCode() {
    }
}
