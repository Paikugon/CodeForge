package com.codeforge.backend.problem.service;

import com.codeforge.backend.problem.dto.ProblemRequest;
import com.codeforge.backend.problem.dto.ProblemListResponse;
import com.codeforge.backend.problem.dto.ProblemResponse;
import com.codeforge.backend.problem.enums.ProblemDifficulty;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.submission.repository.SubmissionRepository;
import com.codeforge.backend.submission.dto.ProblemSubmissionStats;
import com.codeforge.backend.testcase.dto.TestCaseResponse;
import com.codeforge.backend.testcase.event.TestCasesChangedEvent;
import com.codeforge.backend.problem.event.ProblemDeletedEvent;
import com.codeforge.backend.testcase.model.TestCase;
import com.codeforge.backend.testcase.repository.TestCaseRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SubmissionRepository submissionRepository;

    public ProblemService(
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            ApplicationEventPublisher eventPublisher,
            SubmissionRepository submissionRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.eventPublisher = eventPublisher;
        this.submissionRepository = submissionRepository;
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
        problem.setEnabled(request.enabled() == null || request.enabled());
        try {
            return ProblemResponse.from(
                    problemRepository.saveAndFlush(problem),
                    List.of(),
                    true);
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
    public Page<ProblemListResponse> findAll(Pageable pageable, Boolean enabled, boolean isAdmin) {
        return withSubmissionStats(
                problemRepository.findAllSummaries(isAdmin ? enabled : Boolean.TRUE, pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findAll(Pageable pageable) {
        return findAll(pageable, true, false);
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findSolvedByUserId(
            long userId,
            Pageable pageable,
            boolean isAdmin) {
        return withSubmissionStats(
                problemRepository.findSolvedByUserId(userId, isAdmin ? null : Boolean.TRUE, pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findSolvedByUserId(long userId, Pageable pageable) {
        return findSolvedByUserId(userId, pageable, false);
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findUnsolvedByUserId(
            long userId,
            Pageable pageable,
            boolean isAdmin) {
        return withSubmissionStats(
                problemRepository.findUnsolvedByUserId(userId, isAdmin ? null : Boolean.TRUE, pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findUnsolvedByUserId(long userId, Pageable pageable) {
        return findUnsolvedByUserId(userId, pageable, false);
    }

    @Transactional(readOnly = true)
    public ProblemResponse findById(
            Long id,
            boolean includeSpecialJudgeDetails,
            boolean isAdmin) {
        return problemRepository.findById(id)
                .filter(problem -> isAdmin || problem.isEnabled())
                .map(problem -> ProblemResponse.from(
                        problem,
                        testCaseRepository.findSampleByProblemId(problem.getId())
                                .stream()
                                .map(this::toResponse)
                                .toList(),
                        includeSpecialJudgeDetails)
                        .withSubmissionStats(findSubmissionStats(problem.getId())))
                .orElseThrow(() -> new ProblemNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public ProblemResponse findById(Long id, boolean includeSpecialJudgeDetails) {
        return findById(id, includeSpecialJudgeDetails, false);
    }

    private Page<ProblemListResponse> withSubmissionStats(Page<ProblemListResponse> page) {
        if (page.isEmpty()) {
            return page;
        }

        List<Long> problemIds = page.getContent().stream()
                .map(ProblemListResponse::id)
                .toList();
        Map<Long, ProblemSubmissionStats> statsByProblemId = submissionRepository
                .findProblemSubmissionStats(problemIds)
                .stream()
                .collect(Collectors.toMap(ProblemSubmissionStats::problemId, Function.identity()));

        return page.map(problem -> problem.withSubmissionStats(statsByProblemId.get(problem.id())));
    }

    private ProblemSubmissionStats findSubmissionStats(Long problemId) {
        return submissionRepository.findProblemSubmissionStats(List.of(problemId))
                .stream()
                .findFirst()
                .orElse(null);
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
        if (submissionRepository.existsByProblemId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    com.codeforge.backend.common.error.ErrorCode.PROBLEM_HAS_SUBMISSIONS);
        }

        problemRepository.delete(problem);
        eventPublisher.publishEvent(new ProblemDeletedEvent(id));
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
        if (request.enabled() != null) {
            problem.setEnabled(request.enabled());
        }

        try {
            eventPublisher.publishEvent(
                    new TestCasesChangedEvent(problem.getId()));
            return ProblemResponse.from(
                    problemRepository.saveAndFlush(problem),
                    List.of(),
                    true)
                    .withSubmissionStats(findSubmissionStats(problem.getId()));
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
