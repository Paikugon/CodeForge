package com.codeforge.backend.submission.service;

import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.repository.SubmissionRepository;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.codeforge.backend.common.error.ErrorCode.JUDGE_INTERRUPTED;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SubmissionRecoveryRunner implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(SubmissionRecoveryRunner.class);

    private final SubmissionRepository submissionRepository;

    public SubmissionRecoveryRunner(
            SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        int count = submissionRepository.failUnfinishedSubmissions(
                List.of(
                        SubmissionStatus.PENDING,
                        SubmissionStatus.JUDGING),
                SubmissionStatus.FAILED,
                JUDGE_INTERRUPTED,
                OffsetDateTime.now());

        log.info(
                "Marked {} unfinished submissions as failed",
                count);
    }
}