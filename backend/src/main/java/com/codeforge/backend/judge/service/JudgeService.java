package com.codeforge.backend.judge.service;

import com.codeforge.backend.judge.client.JudgeClient;
import com.codeforge.backend.judge.dto.JudgeRequest;
import com.codeforge.backend.judge.dto.JudgeResponse;
import com.codeforge.backend.judge.dto.JudgeResult;
import com.codeforge.backend.judge.dto.JudgeTestCaseResult;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class JudgeService {

    private final JudgeClient judgeClient;
    private final ObjectMapper objectMapper;
    private final JsonNode cpp20Config;
    private final JsonNode java21Config;
    private final JsonNode python3Config;

    public JudgeService(JudgeClient judgeClient, ObjectMapper objectMapper)
            throws IOException {

        this.judgeClient = judgeClient;
        this.objectMapper = objectMapper;

        try (InputStream input = getClass().getResourceAsStream(
                "/judge/cpp20.json")) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing judge/cpp20.json");
            }

            this.cpp20Config = objectMapper.readTree(input);
        }
        try (InputStream input = getClass().getResourceAsStream(
                "/judge/java21.json")) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing judge/java21.json");
            }

            this.java21Config = objectMapper.readTree(input);
        }

        try (InputStream input = getClass().getResourceAsStream(
                "/judge/python3.json")) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing judge/python3.json");
            }

            this.python3Config = objectMapper.readTree(input);
        }
    }

    public JudgeResult judge(
            String language,
            String sourceCode,
            String testCaseId,
            int timeLimitMs,
            int memoryLimitMb) {

        JsonNode languageConfig = switch (language.toLowerCase()) {
            case "cpp" -> cpp20Config;
            case "java" -> java21Config;
            case "python" -> python3Config;
            default -> throw new IllegalArgumentException(
                    "Unsupported language: " + language);
        };

        JudgeRequest request = new JudgeRequest(
                sourceCode,
                languageConfig,
                timeLimitMs,
                (long) memoryLimitMb * 1024 * 1024,
                testCaseId,
                true);

        JudgeResponse response = judgeClient.judge(request);

        if ("CompileError".equals(response.err())) {
            return new JudgeResult(
                    SubmissionVerdict.CE,
                    null,
                    null,
                    0,
                    0,
                    null,
                    null,
                    response.data() != null
                            ? response.data().stringValue()
                            : null);
        }

        if (response.err() != null) {
            throw new IllegalStateException(
                    "Judge Server error: "
                            + response.err()
                            + " - "
                            + response.data());
        }

        if (response.data() == null || !response.data().isArray()) {
            throw new IllegalStateException(
                    "Invalid Judge Server response");
        }

        List<JudgeTestCaseResult> results = objectMapper.convertValue(
                response.data(),
                new TypeReference<List<JudgeTestCaseResult>>() {
                });

        if (results.isEmpty()) {
            throw new IllegalStateException(
                    "Judge Server returned no test case results");
        }

        int passed = 0;
        long maxCpuTime = 0;
        long maxMemory = 0;

        SubmissionVerdict verdict = SubmissionVerdict.AC;
        String failedTestCase = null;
        String failedOutput = null;

        for (JudgeTestCaseResult result : results) {

            maxCpuTime = Math.max(
                    maxCpuTime,
                    result.cpuTime());

            maxMemory = Math.max(
                    maxMemory,
                    result.memory());

            if (result.result() == 0) {
                passed++;
            } else if (verdict == SubmissionVerdict.AC) {
                verdict = mapVerdict(result.result());

                if (failedTestCase == null) {
                    failedTestCase = result.testCase();
                    failedOutput = result.output();
                }
            }
        }

        return new JudgeResult(
                verdict,
                Math.toIntExact(maxCpuTime),
                Math.toIntExact(maxMemory / 1024),
                passed,
                results.size(),
                null,
                failedTestCase,
                failedOutput);
    }

    private SubmissionVerdict mapVerdict(int result) {
        return switch (result) {
            case -1 -> SubmissionVerdict.WA;
            case 1 -> SubmissionVerdict.TLE;
            case 2 -> SubmissionVerdict.TLE;
            case 3 -> SubmissionVerdict.MLE;
            case 4 -> SubmissionVerdict.RE;
            case 5 -> SubmissionVerdict.SE;
            default -> throw new IllegalStateException(
                    "Unknown Judge Server result: " + result);
        };
    }
}