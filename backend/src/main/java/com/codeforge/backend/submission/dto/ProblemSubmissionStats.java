package com.codeforge.backend.submission.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ProblemSubmissionStats(
        Long problemId,
        long judgedSubmissionCount,
        long acceptedSubmissionCount) {

    public BigDecimal acceptanceRate() {
        if (judgedSubmissionCount == 0) {
            return null;
        }

        return BigDecimal.valueOf(acceptedSubmissionCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(judgedSubmissionCount), 2, RoundingMode.HALF_UP);
    }
}
