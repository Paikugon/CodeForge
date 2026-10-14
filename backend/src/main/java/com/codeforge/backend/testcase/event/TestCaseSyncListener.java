package com.codeforge.backend.testcase.event;

import com.codeforge.backend.testcase.service.TestCaseSyncService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TestCaseSyncListener {

    private static final Logger log =
            LoggerFactory.getLogger(TestCaseSyncListener.class);

    private final TestCaseSyncService syncService;

    public TestCaseSyncListener(TestCaseSyncService syncService) {
        this.syncService = syncService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onTestCasesChanged(TestCasesChangedEvent event) {
        try {
            syncService.syncProblem(event.problemId());
        } catch (Exception e) {
            log.error(
                    "Failed to sync test cases for problem {}",
                    event.problemId(),
                    e
            );
        }
    }
}