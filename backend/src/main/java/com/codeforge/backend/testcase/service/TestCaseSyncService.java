package com.codeforge.backend.testcase.service;

import com.codeforge.backend.judge.service.TestCaseStorageService;
import com.codeforge.backend.testcase.model.TestCase;
import com.codeforge.backend.testcase.repository.TestCaseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestCaseSyncService {

    private final TestCaseRepository repository;
    private final TestCaseStorageService storageService;

    public TestCaseSyncService(
            TestCaseRepository repository,
            TestCaseStorageService storageService) {

        this.repository = repository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public void syncProblem(Long problemId) throws IOException {

        List<TestCase> testCases =
                repository.findByProblemIdOrderByOrderIndexAscIdAsc(problemId);

        Set<Long> testCaseIds = testCases.stream()
                .map(TestCase::getId)
                .collect(Collectors.toSet());
        storageService.deleteTestCasesNotIn(problemId, testCaseIds);

        Map<Long, String> expectedOutputs = new LinkedHashMap<>();

        for (TestCase testCase : testCases) {
            storageService.writeTestCase(
                    problemId,
                    testCase.getId(),
                    testCase.getInput(),
                    testCase.getExpectedOutput()
            );

            expectedOutputs.put(
                    testCase.getId(),
                    testCase.getExpectedOutput()
            );
        }

        storageService.writeInfo(problemId, expectedOutputs);
    }
}