package com.codeforge.backend.submission.service;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.model.Submission;
import com.codeforge.backend.submission.repository.SubmissionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.codeforge.backend.common.error.ErrorCode.*;

@Service
public class SubmissionStartService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionStartService(
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository) {

        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Problem start(Submission submission) {
        Problem problem = problemRepository
                .findByIdForTestCaseUpdate(submission.getProblemId())
                .orElseThrow(() ->
                        new IllegalStateException(PROBLEM_NOT_FOUND));

        String directoryId = problem.getActiveTestCaseDirectoryId();

        if (directoryId == null || directoryId.isBlank()) {
            throw new IllegalStateException(
                    PROBLEM_TEST_CASES_NOT_READY);
        }

        submission.setTestCaseDirectoryId(directoryId);
        submission.setStatus(SubmissionStatus.JUDGING);
        submissionRepository.saveAndFlush(submission);

        return problem;
    }
}