package com.codeforge.backend.testcase.service;

import com.codeforge.backend.problem.model.Problem;
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
import java.util.HashSet;
import java.nio.charset.StandardCharsets;
import com.codeforge.backend.testcase.dto.TestCaseLimits;
import com.codeforge.backend.testcase.dto.TestCaseSummary;

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
        validateBatchContent(requests);
        markTestCasesUnsynced(problemId);
        Integer largestExistingIndex = testCaseRepository.findMaxOrderIndexByProblemId(problemId);
        long nextIndex = largestExistingIndex == null ? 0 : (long) largestExistingIndex + 1;
        // Reserve explicit indexes before assigning omitted ones, regardless of array
        // order.
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

        validateBatchContent(List.of(request));
        markTestCasesUnsynced(problemId);

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
        markTestCasesUnsynced(problemId);

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

    public record Status(boolean synced, long totalTestCases) {}

    @Transactional(readOnly = true)
    public Status status(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROBLEM_NOT_FOUND));
        return new Status(problem.isTestCasesSynced(), testCaseRepository.countByProblemId(problemId));
    }

    @Transactional(readOnly = true)
    public Page<TestCaseSummary> summaries(Long problemId, Pageable pageable) {
        validateProblemExists(problemId);
        return testCaseRepository.findSummaries(problemId, pageable);
    }

    @Transactional(readOnly = true)
    public TestCaseResponse getOne(Long problemId, Long id) {
        return testCaseRepository.findByIdAndProblemId(id, problemId).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TEST_CASE_NOT_FOUND));
    }

    @Transactional
    public void updateSampleBatch(Long problemId, List<Long> ids, boolean sample) {
        markTestCasesUnsynced(problemId);
        validateSelection(problemId, ids);
        testCaseRepository.updateSampleBatch(problemId, ids, sample, java.time.OffsetDateTime.now());
        eventPublisher.publishEvent(new TestCasesChangedEvent(problemId));
    }

    @Transactional
    public void deleteBatch(Long problemId, List<Long> ids) {
        markTestCasesUnsynced(problemId);
        validateSelection(problemId, ids);
        testCaseRepository.deleteSelected(problemId, ids);
        eventPublisher.publishEvent(new TestCasesChangedEvent(problemId));
    }

    private void validateSelection(Long problemId, List<Long> ids) {
        if (ids.isEmpty() || ids.size() > 100 || new HashSet<>(ids).size() != ids.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REQUEST_VALIDATION_FAILED);
        if (testCaseRepository.findSelectedIds(problemId, ids).size() != ids.size())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TEST_CASE_NOT_FOUND);
    }

    private void validateBatchContent(List<TestCaseRequest> requests) {
        if (requests == null || requests.isEmpty() || requests.size() > TestCaseLimits.MAX_CASES)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REQUEST_VALIDATION_FAILED);
        long bytes = 0;
        for (var request : requests) {
            if (request == null || request.input() == null || request.expectedOutput() == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REQUEST_VALIDATION_FAILED);
            for (String content : List.of(request.input(), request.expectedOutput())) {
                if (content.indexOf('\0') >= 0)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CODE_INVALID_VALUE");
                int length = content.getBytes(StandardCharsets.UTF_8).length;
                if (length > TestCaseLimits.MAX_FILE_BYTES)
                    throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "CODE_TEST_CASE_IMPORT_TOO_LARGE");
                bytes += length;
            }
        }
        if (bytes > TestCaseLimits.MAX_BATCH_BYTES)
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "CODE_TEST_CASE_IMPORT_TOO_LARGE");
    }

    private void markTestCasesUnsynced(Long problemId) {
        Problem problem = problemRepository
                .findByIdForTestCaseUpdate(problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        PROBLEM_NOT_FOUND));

        problem.setTestCasesSynced(false);
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
