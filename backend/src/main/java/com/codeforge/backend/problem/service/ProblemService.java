package com.codeforge.backend.problem.service;

import com.codeforge.backend.problem.dto.ProblemRequest;
import com.codeforge.backend.problem.dto.ProblemListResponse;
import com.codeforge.backend.problem.dto.ProblemResponse;
import com.codeforge.backend.problem.enums.ProblemDifficulty;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.testcase.dto.TestCaseResponse;
import com.codeforge.backend.testcase.event.TestCasesChangedEvent;
import com.codeforge.backend.testcase.model.TestCase;
import com.codeforge.backend.testcase.repository.TestCaseRepository;

import java.time.OffsetDateTime;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProblemService(
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            ApplicationEventPublisher eventPublisher) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.eventPublisher = eventPublisher;
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
                request.memoryLimitMb() == null ? 256 : request.memoryLimitMb(),
                request.difficulty() == null ? ProblemDifficulty.EASY : request.difficulty());
        applySpj(problem, request);

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
    public Page<ProblemListResponse> findSolvedByUserId(
            long userId,
            Pageable pageable) {
        return problemRepository.findSolvedByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findUnsolvedByUserId(
            long userId,
            Pageable pageable) {
        return problemRepository.findUnsolvedByUserId(userId, pageable);
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

    @Transactional
    public void delete(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemNotFoundException(id));

        problemRepository.delete(problem);
    }

    @Transactional
    public ProblemResponse edit(Long id, ProblemRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemNotFoundException(id));
        if (request.slug() != null && !request.slug().equals(problem.getSlug())) {
            if (problemRepository.existsBySlug(request.slug())) {
                throw new ProblemAlreadyExistsException(request.slug());
            }
            problem.setSlug(request.slug());
        }
        if (request.title() != null && request.title().length() > 0 && !request.title().equals(problem.getTitle())) {
            problem.setTitle(request.title());
        }
        if (request.description() != null && request.description().length() > 0
                && !request.description().equals(problem.getDescription())) {
            problem.setDescription(request.description());
        }
        if (request.timeLimitMs() != null && !request.timeLimitMs().equals(problem.getTimeLimitMs())) {
            problem.setTimeLimitMs(request.timeLimitMs());
        }
        if (request.memoryLimitMb() != null && !request.memoryLimitMb().equals(problem.getMemoryLimitMb())) {
            problem.setMemoryLimitMb(request.memoryLimitMb());
        }
        if (request.difficulty() != null && !request.difficulty().equals(problem.getDifficulty())) {
            problem.setDifficulty(request.difficulty());
        }
        applySpj(problem, request);

        try {
            eventPublisher.publishEvent(
                    new TestCasesChangedEvent(problem.getId()));
            return ProblemResponse.from(
                    problemRepository.saveAndFlush(problem));
        } catch (DataIntegrityViolationException exception) {
            if (isSlugConstraintViolation(exception)) {
                throw new ProblemAlreadyExistsException(request.slug());
            }
            throw exception;
        }
    }

    private void applySpj(
            Problem problem,
            ProblemRequest request) {

        boolean enabled = Boolean.TRUE.equals(request.spjEnabled());

        problem.setSpjEnabled(enabled);
        problem.setSpjSource(enabled ? request.spjSource() : null);
    }
}
