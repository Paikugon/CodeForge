package com.codeforge.backend.problem.event;

import com.codeforge.backend.judge.service.TestCaseStorageService;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProblemDeletedListener {

    private static final Logger log = LoggerFactory.getLogger(ProblemDeletedListener.class);

    private final TestCaseStorageService testCaseStorageService;

    public ProblemDeletedListener(TestCaseStorageService testCaseStorageService) {
        this.testCaseStorageService = testCaseStorageService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProblemDeleted(ProblemDeletedEvent event) {
        try {
            testCaseStorageService.deleteProblemDirectory(event.problemId());
        } catch (IOException | IllegalStateException exception) {
            log.error(
                    "Failed to clean up test cases for deleted problem {}",
                    event.problemId(),
                    exception);
        }
    }
}
