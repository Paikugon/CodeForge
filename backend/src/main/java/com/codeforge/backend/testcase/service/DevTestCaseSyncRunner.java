package com.codeforge.backend.testcase.service;

import com.codeforge.backend.problem.repository.ProblemRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev-seed")
public class DevTestCaseSyncRunner implements ApplicationRunner {
    private final ProblemRepository problemRepository;
    private final TestCaseSyncService syncService;

    public DevTestCaseSyncRunner(ProblemRepository problemRepository, TestCaseSyncService syncService) {
        this.problemRepository = problemRepository;
        this.syncService = syncService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (Long problemId : problemRepository.findAllProblemIds()) {
            // Fail startup if storage cannot be rebuilt, instead of serving unsynced demo data.
            syncService.syncProblem(problemId);
        }
    }
}
