package com.codeforge.backend.submission.service;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.submission.dto.SubmissionRequest;
import com.codeforge.backend.submission.dto.SubmissionListResponse;
import com.codeforge.backend.submission.dto.SubmissionResponse;
import com.codeforge.backend.submission.dto.FailedTestCaseResponse;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import com.codeforge.backend.submission.model.Submission;
import com.codeforge.backend.submission.repository.SubmissionRepository;
import com.codeforge.backend.user.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.task.TaskRejectedException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import static com.codeforge.backend.common.error.ErrorCode.*;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionJudgeService submissionJudgeService;
    private final UserRepository userRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            SubmissionJudgeService submissionJudgeService,
            UserRepository userRepository) {

        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.submissionJudgeService = submissionJudgeService;
        this.userRepository = userRepository;
    }

    public SubmissionResponse create(SubmissionRequest request, long userId) {
        return create(request, userId, false);
    }

    public SubmissionResponse create(SubmissionRequest request, long userId, boolean isAdmin) {
        Problem problem = problemRepository.findById(request.problemId())
                .filter(p -> isAdmin || p.isEnabled())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        PROBLEM_NOT_FOUND));

        if (!request.language().equalsIgnoreCase("cpp")
                && !request.language().equalsIgnoreCase("java")
                && !request.language().equalsIgnoreCase("python")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    UNSUPPORTED_LANGUAGE);
        }

        String directoryId = problem.getActiveTestCaseDirectoryId();
        if (directoryId == null || directoryId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    PROBLEM_TEST_CASES_NOT_READY);
        }

        Submission submission = new Submission(
                request.problemId(),
                userId,
                request.language(),
                request.sourceCode());

        submission.setStatus(SubmissionStatus.PENDING);

        Submission saved = submissionRepository.saveAndFlush(submission);

        try {
            submissionJudgeService.judge(saved.getId());
        } catch (TaskRejectedException exception) {
            saved.setStatus(SubmissionStatus.FAILED);
            saved.setErrorMessage(JUDGE_QUEUE_FULL);

            saved = submissionRepository.saveAndFlush(saved);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getById(Long id, long userId) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, SUBMISSION_NOT_FOUND));
        boolean isOwner = Long.valueOf(userId).equals(submission.getUserId());
        return toResponse(submission, isOwner);
    }

    @Transactional(readOnly = true)
    public Page<SubmissionListResponse> getAll(
            Long problemId,
            Pageable pageable) {
        if (problemId == null) {
            return submissionRepository.findAllSummaries(pageable);
        }

        return submissionRepository.findSummaryByProblemId(problemId, pageable);
    }

    private SubmissionResponse toResponse(Submission submission) {
        return toResponse(submission, true);
    }

    private SubmissionResponse toResponse(
            Submission submission,
            boolean includePrivateInfo) {
        String username = userRepository.findById(submission.getUserId())
                .map(user -> user.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "User not found for submission: " + submission.getId()));

        return new SubmissionResponse(
                submission.getId(),
                submission.getUserId(),
                username,
                submission.getProblemId(),
                submission.getLanguage(),
                includePrivateInfo ? submission.getSourceCode() : null,
                submission.getStatus(),
                submission.getVerdict(),
                submission.getExecutionTimeMs(),
                submission.getMemoryUsedKb(),
                submission.getPassedTestCases(),
                submission.getTotalTestCases(),
                submission.getCompileOutput(),
                includePrivateInfo ? submission.getErrorMessage() : null,
                includePrivateInfo ? submission.getFailedOutput() : null,
                includePrivateInfo ? toFailedTestCaseResponse(submission) : null,
                submission.getCreatedAt(),
                submission.getUpdatedAt());
    }

    private FailedTestCaseResponse toFailedTestCaseResponse(
            Submission submission) {

        if (submission.getFailedTestCase() == null) {
            return null;
        }

        return new FailedTestCaseResponse(
                submission.getFailedTestCase(),
                submission.getFailedInput(),
                submission.getFailedExpectedOutput(),
                submission.getFailedOutput());
    }

    @Transactional(readOnly = true)
    public Page<SubmissionListResponse> getMine(
            long userId,
            Long problemId,
            SubmissionVerdict verdict,
            Pageable pageable) {

        return submissionRepository.findSummariesByUserId(
                userId, problemId, verdict, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SubmissionListResponse> getByUserId(
            long userId,
            Long problemId,
            SubmissionVerdict verdict,
            Pageable pageable) {
        return submissionRepository.findSummariesByUserId(
                userId, problemId, verdict, pageable);
    }
}
