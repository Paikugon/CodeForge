package com.codeforge.backend.testcase.service;

import com.codeforge.backend.judge.service.TestCaseStorageService;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.repository.SubmissionRepository;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Cleaning up unused test case snapshots that are no longer needed by any submissions
@Service
public class SnapshotCleanupService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final TestCaseStorageService storageService;

    public SnapshotCleanupService(
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository,
            TestCaseStorageService storageService) {

        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.storageService = storageService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void cleanupUnused(
            Long problemId,
            String directoryId) throws IOException {

        if (directoryId == null || directoryId.isBlank()) {
            return;
        }

        String prefix = storageService.getTestCaseDirectoryId(problemId) + "_";

        if (!directoryId.startsWith(prefix)) {
            throw new IllegalArgumentException(
                    "Snapshot does not belong to problem");
        }

        Problem problem = problemRepository
                .findByIdForTestCaseUpdate(problemId)
                .orElse(null);

        if (problem == null) {
            return;
        }


        if (directoryId.equals(
                problem.getActiveTestCaseDirectoryId())) {
            return;
        }

        boolean stillNeeded = submissionRepository
                .existsByTestCaseDirectoryIdAndStatusNot(
                        directoryId,
                        SubmissionStatus.COMPLETED);

        if (stillNeeded) {
            return;
        }
        storageService.deleteSnapshotDirectory(directoryId);
    }
}