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
import static com.codeforge.backend.common.error.ErrorCode.*;
import java.util.ArrayList;
import java.util.List;

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
        return createBatch(problemId, List.of(request)).getFirst();
    }

    @Transactional
    public List<TestCaseResponse> createBatch(Long problemId, List<TestCaseRequest> requests) {
        lockProblem(problemId);
        Integer largestExistingIndex = testCaseRepository.findMaxOrderIndexByProblemId(problemId);
        long nextIndex = largestExistingIndex == null ? 0 : (long) largestExistingIndex + 1;
        // Reserve explicit indexes before assigning omitted ones, regardless of array order.
        for (TestCaseRequest request : requests) {
            if (request.orderIndex() != null) {
                nextIndex = Math.max(nextIndex, (long) request.orderIndex() + 1);
            }
        }

        List<TestCase> testCases = new ArrayList<>();
        for (TestCaseRequest request : requests) {
            int orderIndex;
            if (request.orderIndex() != null) {
                orderIndex = request.orderIndex();
            } else {
                if (nextIndex > Integer.MAX_VALUE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEST_CASE_ORDER_EXHAUSTED);
                }
                orderIndex = (int) nextIndex++;
            }
            testCases.add(new TestCase(problemId, request.input(), request.expectedOutput(),
                    Boolean.TRUE.equals(request.isSample()), orderIndex));
        }

        List<TestCase> saved = testCaseRepository.saveAllAndFlush(testCases);
        eventPublisher.publishEvent(new TestCasesChangedEvent(problemId));
        return saved.stream().map(this::toResponse).toList();
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

        lockProblem(problemId);

        TestCase testCase = testCaseRepository
                .findByIdAndProblemId(testCaseId, problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        TEST_CASE_NOT_FOUND));

        testCase.update(
                request.input(),
                request.expectedOutput(),
                request.isSample() != null ? request.isSample() : testCase.getIsSample(),
                request.orderIndex() != null ? request.orderIndex() : testCase.getOrderIndex());

        TestCase saved = testCaseRepository.saveAndFlush(testCase);
        eventPublisher.publishEvent(new TestCasesChangedEvent(problemId));

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long problemId, Long testCaseId) {
        lockProblem(problemId);

        TestCase testCase = testCaseRepository
                .findByIdAndProblemId(testCaseId, problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        TEST_CASE_NOT_FOUND));

        testCaseRepository.delete(testCase);
        testCaseRepository.flush();
        eventPublisher.publishEvent(new TestCasesChangedEvent(problemId));
    }

    private void validateProblemExists(Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    PROBLEM_NOT_FOUND);
        }
    }

    private void lockProblem(Long problemId) {
        problemRepository.findByIdForTestCaseUpdate(problemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROBLEM_NOT_FOUND));
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
