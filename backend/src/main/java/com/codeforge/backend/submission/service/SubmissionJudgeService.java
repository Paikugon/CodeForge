package com.codeforge.backend.submission.service;

import com.codeforge.backend.judge.dto.JudgeResult;
import com.codeforge.backend.judge.service.JudgeService;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.model.Submission;
import com.codeforge.backend.submission.repository.SubmissionRepository;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SubmissionJudgeService {

    private final SubmissionRepository submissionRepository;
    private final JudgeService judgeService;

    public SubmissionJudgeService(
            SubmissionRepository submissionRepository,
            JudgeService judgeService) {

        this.submissionRepository = submissionRepository;
        this.judgeService = judgeService;
    }

    @Async("judgeExecutor")
    public void judge(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Submission not found: " + submissionId));

        try {
            submission.setStatus(SubmissionStatus.JUDGING);
            submissionRepository.saveAndFlush(submission);

            String testCaseId =
                    "codeforge_" + submission.getProblemId();

            JudgeResult result = judgeService.judgeCpp20(
                    submission.getSourceCode(),
                    testCaseId
            );

            submission.setVerdict(result.verdict());
            submission.setExecutionTimeMs(result.executionTimeMs());
            submission.setMemoryUsedKb(result.memoryUsedKb());
            submission.setPassedTestCases(result.passedTestCases());
            submission.setTotalTestCases(result.totalTestCases());
            submission.setCompileOutput(result.compileOutput());
            submission.setErrorMessage(null);

            submission.setStatus(SubmissionStatus.COMPLETED);

        } catch (Exception e) {
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setErrorMessage(e.getMessage());
        }

        submissionRepository.saveAndFlush(submission);
    }
}
