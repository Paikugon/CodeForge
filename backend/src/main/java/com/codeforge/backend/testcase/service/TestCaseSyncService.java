package com.codeforge.backend.testcase.service;

import com.codeforge.backend.judge.service.TestCaseStorageService;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.problem.service.ProblemNotFoundException;
import com.codeforge.backend.testcase.model.TestCase;
import com.codeforge.backend.testcase.repository.TestCaseRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.codeforge.backend.common.error.ErrorCode.PROBLEM_HAS_NO_TEST_CASES;

@Service
public class TestCaseSyncService {

    private final TestCaseRepository repository;
    private final TestCaseStorageService storageService;
    private final ProblemRepository problemRepository;

    public TestCaseSyncService(
            TestCaseRepository repository,
            TestCaseStorageService storageService,
            ProblemRepository problemRepository) {

        this.repository = repository;
        this.storageService = storageService;
        this.problemRepository = problemRepository;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void syncProblem(Long problemId) throws IOException {

        Problem problem = problemRepository
                .findByIdForTestCaseUpdate(problemId)
                .orElseThrow(() -> new ProblemNotFoundException(problemId));

        List<TestCase> testCases = repository
                .findByProblemIdOrderByOrderIndexAscIdAsc(problemId);

        if (testCases.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    PROBLEM_HAS_NO_TEST_CASES);
        }

        String directoryId = storageService.getTestCaseDirectoryId(problemId)
                + "_" + UUID.randomUUID();

        Map<Long, String> expectedOutputs = new LinkedHashMap<>();

        for (TestCase testCase : testCases) {
            storageService.writeTestCase(
                    directoryId,
                    testCase.getId(),
                    testCase.getInput(),
                    testCase.getExpectedOutput());

            expectedOutputs.put(
                    testCase.getId(),
                    testCase.getExpectedOutput());
        }

        /* test: force sync failure
        boolean simulateFailure = testCases.stream()
                .anyMatch(testCase -> "__FORCE_SYNC_FAILURE__".equals(testCase.getInput()));

        if (simulateFailure) {
            throw new IOException("Simulated test case sync failure");
        } */

        storageService.writeInfo(
                directoryId,
                expectedOutputs,
                problem.isSpjEnabled());

        // Chỉ chuyển sang bộ mới sau khi ghi thành công toàn bộ file.
        problem.setActiveTestCaseDirectoryId(directoryId);
        problem.setActiveSpjSource(
                problem.isSpjEnabled() ? problem.getSpjSource() : null);
        problem.setTestCasesSynced(true);
    }
}