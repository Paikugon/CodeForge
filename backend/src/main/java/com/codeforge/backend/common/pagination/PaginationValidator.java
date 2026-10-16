package com.codeforge.backend.common.pagination;

public final class PaginationValidator {

    private PaginationValidator() {
    }

    public static void validate(String page, String size, int maxSize) {
        if (page != null) {
            int pageNumber = parse(page, "Page");
            if (pageNumber < 0) {
                throw new IllegalArgumentException(
                        "Page must be greater than or equal to 0");
            }
        }

        if (size != null) {
            int pageSize = parse(size, "Size");
            if (pageSize <= 0) {
                throw new IllegalArgumentException(
                        "Size must be greater than 0");
            }
            if (pageSize > maxSize) {
                throw new IllegalArgumentException(
                        "Size must be less than or equal to " + maxSize);
            }
        }
    }

    private static int parse(String value, String parameterName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    parameterName + " must be a valid integer");
        }
    }
}
