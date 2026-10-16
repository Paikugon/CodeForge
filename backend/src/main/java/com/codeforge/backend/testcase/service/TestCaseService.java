package com.codeforge.backend.testcase.service;

import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.testcase.dto.TestCaseRequest;
import com.codeforge.backend.testcase.dto.TestCaseResponse;
import com.codeforge.backend.testcase.model.TestCase;
import com.codeforge.backend.testcase.repository.TestCaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.codeforge.backend.testcase.event.TestCasesChangedEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TestCaseService(
            TestCaseRepository testCaseRepository,
            ProblemRepository problemRepository,
            ApplicationEventPublisher eventPublisher) {

        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TestCaseResponse create(Long problemId, TestCaseRequest request) {
        validateProblemExists(problemId);

        TestCase testCase = new TestCase(
                problemId,
                request.input(),
                request.expectedOutput(),
                request.isSample() != null ? request.isSample() : false,
                request.orderIndex() != null ? request.orderIndex() : 0);

        TestCase saved = testCaseRepository.saveAndFlush(testCase);

        eventPublisher.publishEvent(
                new TestCasesChangedEvent(problemId));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TestCaseResponse> findAll(Long problemId, Pageable pageable) {
        validateProblemExists(problemId);

        return testCaseRepository.findByProblemId(problemId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public TestCaseResponse update(
            Long problemId,
            Long testCaseId,
            TestCaseRequest request) {

        validateProblemExists(problemId);

        TestCase testCase = testCaseRepository
                .findByIdAndProblemId(testCaseId, problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Test case not found: " + testCaseId));

        testCase.update(
                request.input(),
                request.expectedOutput(),
                request.isSample() != null ? request.isSample() : false,
                request.orderIndex() != null ? request.orderIndex() : 0);

        TestCase saved = testCaseRepository.saveAndFlush(testCase);
        eventPublisher.publishEvent(new TestCasesChangedEvent(problemId));

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long problemId, Long testCaseId) {
        validateProblemExists(problemId);

        TestCase testCase = testCaseRepository
                .findByIdAndProblemId(testCaseId, problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Test case not found: " + testCaseId));

        testCaseRepository.delete(testCase);
        testCaseRepository.flush();
        eventPublisher.publishEvent(new TestCasesChangedEvent(problemId));
    }

    private void validateProblemExists(Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Problem not found: " + problemId);
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
                testCase.getUpdatedAt());
    }
}