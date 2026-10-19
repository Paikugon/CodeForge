package com.codeforge.backend.problem.service;

import com.codeforge.backend.judge.dto.RunCodeResult;
import com.codeforge.backend.judge.service.JudgeService;
import com.codeforge.backend.problem.dto.RunCodeRequest;
import com.codeforge.backend.problem.dto.RunCodeResponse;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RunCodeService {

    private final ProblemRepository problemRepository;
    private final JudgeService judgeService;

    public RunCodeService(
            ProblemRepository problemRepository,
            JudgeService judgeService) {

        this.problemRepository = problemRepository;
        this.judgeService = judgeService;
    }

    public RunCodeResponse run(Long problemId, RunCodeRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Problem not found"));

        String language = request.language().toLowerCase();

        if (!language.equals("cpp")
                && !language.equals("java")
                && !language.equals("python")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported language");
        }

        RunCodeResult result = judgeService.run(
                language,
                request.sourceCode(),
                request.input(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb()
        );

        return new RunCodeResponse(
                result.resultCode(),
                result.stdout(),
                result.exitCode(),
                result.executionTimeMs(),
                result.memoryUsedKb(),
                result.compileOutput()
        );
    }
}