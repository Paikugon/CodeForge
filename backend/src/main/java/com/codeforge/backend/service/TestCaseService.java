package com.codeforge.backend.service;

import com.codeforge.backend.dto.TestCaseRequest;
import com.codeforge.backend.dto.TestCaseResponse;
import com.codeforge.backend.entity.TestCase;
import com.codeforge.backend.repository.ProblemRepository;
import com.codeforge.backend.repository.TestCaseRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public TestCaseService(
            TestCaseRepository testCaseRepository,
            ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional
    public TestCaseResponse create(Long problemId, TestCaseRequest request) {
        validateProblemExists(problemId);

        TestCase testCase = new TestCase(
            problemId,
            request.input(),
            request.expectedOutput(),
            request.isSample() != null ? request.isSample() : false,
            request.orderIndex() != null ? request.orderIndex() : 0
        );

        return toResponse(testCaseRepository.saveAndFlush(testCase));
    }

    @Transactional(readOnly = true)
    public List<TestCaseResponse> findAll(Long problemId) {
        validateProblemExists(problemId);

        return testCaseRepository
            .findByProblemIdOrderByOrderIndexAscIdAsc(problemId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private void validateProblemExists(Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Problem not found: " + problemId
            );
        }
    }

    private TestCaseResponse toResponse(TestCase testCase) {
        return new TestCaseResponse(
            testCase.getId(),
            testCase.getProblemId(),
            testCase.getInput(),
            testCase.getExpectedOutput(),
            testCase.getIsSample(),
            testCase.getOrderIndex(),
            testCase.getCreatedAt(),
            testCase.getUpdatedAt()
        );
    }
}