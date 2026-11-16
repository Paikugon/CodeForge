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
import com.codeforge.backend.testcase.service.TestCaseService;
import com.codeforge.backend.tag.model.Tag;
import com.codeforge.backend.tag.repository.TagRepository;
import com.codeforge.backend.tag.dto.TagResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
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

import static com.codeforge.backend.common.error.ErrorCode.TAG_NOT_FOUND;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SubmissionRepository submissionRepository;
    private final TagRepository tagRepository;
    private final TestCaseService testCaseService;

    public ProblemService(
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            ApplicationEventPublisher eventPublisher,
            SubmissionRepository submissionRepository,
            TagRepository tagRepository,
            TestCaseService testCaseService) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.eventPublisher = eventPublisher;
        this.submissionRepository = submissionRepository;
        this.tagRepository = tagRepository;
        this.testCaseService = testCaseService;
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
        replaceTags(problem, request.tagIds());
        try {
            Problem saved = problemRepository.saveAndFlush(problem);
            List<TestCaseResponse> testCases = request.testCases() == null || request.testCases().isEmpty()
                    ? List.of()
                    : testCaseService.createBatch(saved.getId(), request.testCases());
            return ProblemResponse.from(
                    saved,
                    testCases,
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
    public Page<ProblemListResponse> findAll(
            Pageable pageable,
            Boolean enabled,
            ProblemDifficulty difficulty,
            List<Long> tagIds,
            boolean isAdmin) {
        List<Long> normalizedTagIds = normalizeTagIds(tagIds);
        return withSubmissionStats(
                problemRepository.findAllSummaries(
                        isAdmin ? enabled : Boolean.TRUE,
                        difficulty,
                        normalizedTagIds,
                        normalizedTagIds == null ? 0 : normalizedTagIds.size(),
                        pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findAll(Pageable pageable) {
        return findAll(pageable, true, null, null, false);
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findAll(
            Pageable pageable,
            Boolean enabled,
            boolean isAdmin) {
        return findAll(pageable, enabled, null, null, isAdmin);
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findSolvedByUserId(
            long userId,
            Pageable pageable,
            ProblemDifficulty difficulty,
            List<Long> tagIds,
            boolean isAdmin) {
        List<Long> normalizedTagIds = normalizeTagIds(tagIds);
        return withSubmissionStats(
                problemRepository.findSolvedByUserId(
                        userId,
                        isAdmin ? null : Boolean.TRUE,
                        difficulty,
                        normalizedTagIds,
                        normalizedTagIds == null ? 0 : normalizedTagIds.size(),
                        pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findSolvedByUserId(long userId, Pageable pageable) {
        return findSolvedByUserId(userId, pageable, null, null, false);
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findUnsolvedByUserId(
            long userId,
            Pageable pageable,
            ProblemDifficulty difficulty,
            List<Long> tagIds,
            boolean isAdmin) {
        List<Long> normalizedTagIds = normalizeTagIds(tagIds);
        return withSubmissionStats(
                problemRepository.findUnsolvedByUserId(
                        userId,
                        isAdmin ? null : Boolean.TRUE,
                        difficulty,
                        normalizedTagIds,
                        normalizedTagIds == null ? 0 : normalizedTagIds.size(),
                        pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProblemListResponse> findUnsolvedByUserId(long userId, Pageable pageable) {
        return findUnsolvedByUserId(userId, pageable, null, null, false);
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return null;
        }
        return tagIds.stream().distinct().toList();
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
        Map<Long, List<TagResponse>> tagsByProblemId = problemRepository
                .findAllWithTagsByIdIn(problemIds)
                .stream()
                .collect(Collectors.toMap(
                        Problem::getId,
                        problem -> problem.getTags().stream().map(TagResponse::from).toList()));
        Map<Long, ProblemSubmissionStats> statsByProblemId = submissionRepository
                .findProblemSubmissionStats(problemIds)
                .stream()
                .collect(Collectors.toMap(ProblemSubmissionStats::problemId, Function.identity()));

        return page.map(problem -> problem
                .withTags(tagsByProblemId.getOrDefault(problem.id(), List.of()))
                .withSubmissionStats(statsByProblemId.get(problem.id())));
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
        if (request.testCases() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    com.codeforge.backend.common.error.ErrorCode.TEST_CASES_CREATE_ONLY);
        }
        Problem problem = problemRepository.findByIdForTestCaseUpdate(id)
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
        if (request.tagIds() != null) {
            replaceTags(problem, request.tagIds());
        }

        try {
            problem.setTestCasesSynced(false);
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

    private void replaceTags(Problem problem, Set<Long> tagIds) {
        Set<Long> requestedIds = tagIds == null ? Set.of() : new LinkedHashSet<>(tagIds);
        List<Tag> tags = tagRepository.findAllById(requestedIds);
        if (tags.size() != requestedIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TAG_NOT_FOUND);
        }
        problem.getTags().clear();
        problem.getTags().addAll(tags);
    }
}
