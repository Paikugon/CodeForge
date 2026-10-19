package com.codeforge.backend.submission.service;

import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.submission.dto.SubmissionRequest;
import com.codeforge.backend.submission.dto.SubmissionListResponse;
import com.codeforge.backend.submission.dto.SubmissionResponse;
import com.codeforge.backend.submission.dto.FailedTestCaseResponse;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.model.Submission;
import com.codeforge.backend.submission.repository.SubmissionRepository;
import com.codeforge.backend.testcase.model.TestCase;
import com.codeforge.backend.testcase.repository.TestCaseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionJudgeService submissionJudgeService;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            SubmissionJudgeService submissionJudgeService,
            TestCaseRepository testCaseRepository) {

        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.submissionJudgeService = submissionJudgeService;
        this.testCaseRepository = testCaseRepository;
    }

    public SubmissionResponse create(SubmissionRequest request) {

        if (!problemRepository.existsById(request.problemId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Problem not found");
        }

        if (!request.language().equalsIgnoreCase("cpp")
                && !request.language().equalsIgnoreCase("java")
                && !request.language().equalsIgnoreCase("python")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported language");
        }

        if (testCaseRepository.countByProblemId(request.problemId()) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Problem has no test cases");
        }

        Submission submission = new Submission(
                request.problemId(),
                request.language(),
                request.sourceCode());

        submission.setStatus(SubmissionStatus.PENDING);

        Submission saved = submissionRepository.saveAndFlush(submission);

        submissionJudgeService.judge(saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Submission not found"));

        return toResponse(submission);
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
        return new SubmissionResponse(
                submission.getId(),
                submission.getProblemId(),
                submission.getLanguage(),
                submission.getSourceCode(),
                submission.getStatus(),
                submission.getVerdict(),
                submission.getExecutionTimeMs(),
                submission.getMemoryUsedKb(),
                submission.getPassedTestCases(),
                submission.getTotalTestCases(),
                submission.getCompileOutput(),
                submission.getErrorMessage(),
                toFailedTestCaseResponse(submission),
                submission.getCreatedAt(),
                submission.getUpdatedAt());
    }

    private FailedTestCaseResponse toFailedTestCaseResponse(
            Submission submission) {
        if (submission.getFailedTestCase() == null) {
            return null;
        }

        Long testCaseId = parseTestCaseId(submission.getFailedTestCase());
        TestCase testCase = testCaseRepository
                .findByIdAndProblemId(testCaseId, submission.getProblemId())
                .orElse(null);

        if (testCase == null) {
            return new FailedTestCaseResponse(
                    submission.getFailedTestCase(),
                    null,
                    null,
                    submission.getFailedOutput());
        }

        return new FailedTestCaseResponse(
                submission.getFailedTestCase(),
                testCase.getInput(),
                testCase.getExpectedOutput(),
                submission.getFailedOutput());
    }

    private Long parseTestCaseId(String testCase) {
        String fileName = testCase.substring(
                testCase.lastIndexOf('/') + 1);
        int extensionIndex = fileName.lastIndexOf('.');
        String id = extensionIndex > 0
                ? fileName.substring(0, extensionIndex)
                : fileName;

        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Invalid failed test case identifier: " + testCase,
                    e);
        }
    }
}
