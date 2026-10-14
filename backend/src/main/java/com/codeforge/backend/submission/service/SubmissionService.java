package com.codeforge.backend.submission.service;

import com.codeforge.backend.judge.service.JudgeService;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.submission.dto.SubmissionRequest;
import com.codeforge.backend.submission.dto.SubmissionResponse;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.model.Submission;
import com.codeforge.backend.submission.repository.SubmissionRepository;
import com.codeforge.backend.testcase.repository.TestCaseRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.codeforge.backend.judge.dto.JudgeResult;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final JudgeService judgeService;
    private final TestCaseRepository testCaseRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            JudgeService judgeService,
            TestCaseRepository testCaseRepository) {

        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.judgeService = judgeService;
        this.testCaseRepository = testCaseRepository;
    }

    public SubmissionResponse create(SubmissionRequest request) {
        if (!problemRepository.existsById(request.problemId())) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Problem not found");
        }

        if (!request.language().equalsIgnoreCase("cpp")) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Unsupported language");
        }

        if (testCaseRepository.countByProblemId(request.problemId()) == 0) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Problem has no test cases");
        }

        Submission submission = new Submission(
                request.problemId(),
                "cpp",
                request.sourceCode());

        submission.setStatus(SubmissionStatus.PENDING);
        submission = submissionRepository.saveAndFlush(submission);

        try {
            submission.setStatus(SubmissionStatus.JUDGING);
            submission = submissionRepository.saveAndFlush(submission);

            String testCaseId = "codeforge_" + request.problemId();

            JudgeResult result = judgeService.judgeCpp20(
                    request.sourceCode(),
                    testCaseId);

            submission.setVerdict(result.verdict());
            submission.setExecutionTimeMs(result.executionTimeMs());
            submission.setMemoryUsedKb(result.memoryUsedKb());
            submission.setPassedTestCases(result.passedTestCases());
            submission.setTotalTestCases(result.totalTestCases());
            submission.setCompileOutput(result.compileOutput());
            submission.setStatus(SubmissionStatus.COMPLETED);

        } catch (Exception e) {
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setErrorMessage(e.getMessage());
        }

        return toResponse(
                submissionRepository.saveAndFlush(submission));
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Submission not found"));

        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getAll(Long problemId) {
        List<Submission> submissions;

        if (problemId == null) {
            submissions = submissionRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            submissions = submissionRepository
                    .findByProblemIdOrderByCreatedAtDesc(problemId);
        }

        return submissions.stream()
                .map(this::toResponse)
                .toList();
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
                submission.getCreatedAt(),
                submission.getUpdatedAt());
    }
}
