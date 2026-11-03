package com.codeforge.backend.common.pagination;

public final class PaginationValidator {

    private PaginationValidator() {
    }

    public static void validate(String page, String size, int maxSize) {
        if (page != null) {
            int pageNumber = parse(page, "Page");
            if (pageNumber < 0) {
                throw new IllegalArgumentException(
                        "CODE_PAGE_MUST_BE_NON_NEGATIVE");
            }
        }

        if (size != null) {
            int pageSize = parse(size, "Size");
            if (pageSize <= 0) {
                throw new IllegalArgumentException(
                        "CODE_PAGE_SIZE_MUST_BE_POSITIVE");
            }
            if (pageSize > maxSize) {
                throw new IllegalArgumentException(
                        "CODE_PAGE_SIZE_EXCEEDS_MAXIMUM");
            }
        }
    }

    private static int parse(String value, String parameterName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "CODE_INVALID_PAGE_PARAMETER");
        }
    }
}
