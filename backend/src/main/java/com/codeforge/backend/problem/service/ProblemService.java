package com.codeforge.backend.problem.service;

import com.codeforge.backend.problem.dto.ProblemRequest;
import com.codeforge.backend.problem.dto.ProblemListResponse;
import com.codeforge.backend.problem.dto.ProblemResponse;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.testcase.dto.TestCaseResponse;
import com.codeforge.backend.testcase.model.TestCase;
import com.codeforge.backend.testcase.repository.TestCaseRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemService(
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Transactional
    public ProblemResponse create(ProblemRequest request) {
        if (problemRepository.existsBySlug(request.slug())) {
            throw new ProblemAlreadyExistsException(request.slug());
        }

        Problem problem = new Problem(
                request.title(),
                request.slug(),
                request.description(),
                request.timeLimitMs() == null ? 1000 : request.timeLimitMs(),
                request.memoryLimitMb() == null ? 256 : request.memoryLimitMb());

        try {
            return ProblemResponse.from(problemRepository.saveAndFlush(problem));
        } catch (DataIntegrityViolationException ex) {
            if (isSlugConstraintViolation(ex)) {
                throw new ProblemAlreadyExistsException(request.slug());
            }

            throw ex;
        }
    }

    private boolean isSlugConstraintViolation(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintEx
                    && "uk_problems_slug".equals(
                            constraintEx.getConstraintName())) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findAll(Pageable pageable) {
        return problemRepository.findAllSummaries(pageable);
    }

    @Transactional(readOnly = true)
    public ProblemResponse findById(Long id) {
        return problemRepository.findById(id)
                .map(problem -> ProblemResponse.from(
                        problem,
                        testCaseRepository.findSampleByProblemId(problem.getId())
                                .stream()
                                .map(this::toResponse)
                                .toList()))
                .orElseThrow(() -> new ProblemNotFoundException(id));
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
