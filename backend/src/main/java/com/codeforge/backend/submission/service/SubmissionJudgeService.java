package com.codeforge.backend.submission.service;

import com.codeforge.backend.judge.dto.JudgeResult;
import com.codeforge.backend.judge.service.JudgeService;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.model.Submission;
import com.codeforge.backend.submission.repository.SubmissionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.codeforge.backend.common.error.ErrorCode.*;

@Service
public class SubmissionJudgeService {

    private final SubmissionRepository submissionRepository;
    private final JudgeService judgeService;
    private final ProblemRepository problemRepository;
    private static final Logger log = LoggerFactory.getLogger(SubmissionJudgeService.class);

    public SubmissionJudgeService(
            SubmissionRepository submissionRepository,
            JudgeService judgeService,
            ProblemRepository problemRepository) {

        this.submissionRepository = submissionRepository;
        this.judgeService = judgeService;
        this.problemRepository = problemRepository;
    }

    @Async("judgeExecutor")
    public void judge(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElse(null);

        if (submission == null) {
            log.warn("Submission {} no longer exists", submissionId);
            return;
        }

        try {
            Problem problem = problemRepository.findById(submission.getProblemId())
                    .orElseThrow(() -> new IllegalStateException(
                            PROBLEM_NOT_FOUND));
            submission.setStatus(SubmissionStatus.JUDGING);
            submissionRepository.saveAndFlush(submission);

            String testCaseId = "codeforge_" + submission.getProblemId();

            JudgeResult result = judgeService.judge(
                    submission.getLanguage().toString(),
                    submission.getSourceCode(),
                    testCaseId,
                    problem.getTimeLimitMs(),
                    problem.getMemoryLimitMb());

            submission.setVerdict(result.verdict());
            submission.setExecutionTimeMs(result.executionTimeMs());
            submission.setMemoryUsedKb(result.memoryUsedKb());
            submission.setPassedTestCases(result.passedTestCases());
            submission.setTotalTestCases(result.totalTestCases());
            submission.setCompileOutput(result.compileOutput());
            submission.setFailedTestCase(result.failedTestCase());
            submission.setFailedOutput(result.failedOutput());
            submission.setErrorMessage(null);

            submission.setStatus(SubmissionStatus.COMPLETED);

        } catch (Exception e) {
            log.error("Error occurred while judging submission {}", submissionId, e);
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setErrorMessage(JUDGE_FAILED);
        }

        submissionRepository.saveAndFlush(submission);
    }
}
