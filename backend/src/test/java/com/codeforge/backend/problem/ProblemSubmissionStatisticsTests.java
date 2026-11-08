package com.codeforge.backend.problem;

import com.codeforge.backend.problem.dto.ProblemListResponse;
import com.codeforge.backend.problem.dto.ProblemResponse;
import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.problem.service.ProblemNotFoundException;
import com.codeforge.backend.problem.service.ProblemService;
import com.codeforge.backend.submission.dto.ProblemSubmissionStats;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import com.codeforge.backend.submission.model.Submission;
import com.codeforge.backend.submission.repository.SubmissionRepository;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class ProblemSubmissionStatisticsTests {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;
    private int problemNumber;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @Test
    void aggregatesEligibleResultsAcrossUsersAndOnlyForRequestedProblems() {
        Problem first = problem(true);
        Problem second = problem(true);
        Problem unrequested = problem(true);
        for (SubmissionVerdict verdict : List.of(
                SubmissionVerdict.AC, SubmissionVerdict.WA, SubmissionVerdict.TLE,
                SubmissionVerdict.MLE, SubmissionVerdict.RE, SubmissionVerdict.CE)) {
            submission(first, 10L, SubmissionStatus.COMPLETED, verdict);
        }
        submission(first, 20L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);
        submission(first, 10L, SubmissionStatus.PENDING, null);
        submission(first, 10L, SubmissionStatus.JUDGING, SubmissionVerdict.AC);
        submission(first, 10L, SubmissionStatus.FAILED, SubmissionVerdict.AC);
        submission(first, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.SE);
        submission(first, 10L, SubmissionStatus.COMPLETED, null);
        submission(second, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);
        submission(unrequested, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);

        List<ProblemSubmissionStats> results = submissionRepository.findProblemSubmissionStats(
                List.of(first.getId(), second.getId()));

        assertEquals(2, results.size());
        ProblemSubmissionStats firstStats = results.stream()
                .filter(result -> result.problemId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertEquals(7, firstStats.judgedSubmissionCount());
        assertEquals(2, firstStats.acceptedSubmissionCount());
        assertEquals(new BigDecimal("28.57"), firstStats.acceptanceRate());
        assertTrue(results.stream().noneMatch(result -> result.problemId().equals(unrequested.getId())));
    }

    @Test
    void enrichesOnePageWithOneAggregateQueryAndPreservesPagination() {
        Problem first = problem(true);
        Problem second = problem(true);
        problem(true);
        Problem hidden = problem(false);
        submission(first, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);
        submission(first, 20L, SubmissionStatus.COMPLETED, SubmissionVerdict.WA);
        submission(first, 20L, SubmissionStatus.COMPLETED, SubmissionVerdict.CE);
        submission(second, 10L, SubmissionStatus.JUDGING, null);
        submission(second, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.SE);
        submission(hidden, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);
        statistics.clear();

        Page<ProblemListResponse> page = problemService.findAll(
                PageRequest.of(0, 2, Sort.by("id")));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(List.of(first.getId(), second.getId()),
                page.getContent().stream().map(ProblemListResponse::id).toList());
        assertEquals(1, page.getContent().getFirst().acceptedSubmissionCount());
        assertEquals(3, page.getContent().getFirst().judgedSubmissionCount());
        assertEquals(new BigDecimal("33.33"), page.getContent().getFirst().acceptanceRate());
        assertEquals(0, page.getContent().get(1).judgedSubmissionCount());
        assertEquals(0, page.getContent().get(1).acceptedSubmissionCount());
        assertNull(page.getContent().get(1).acceptanceRate());
        assertEquals(1, aggregateQueryExecutions());
        assertEquals(3, statistics.getQueryExecutionCount(),
                "Expected the page query, pagination count, and one statistics query.");
    }

    @Test
    void skipsStatisticsQueryWhenRequestedPageIsEmpty() {
        problem(true);
        statistics.clear();

        Page<ProblemListResponse> page = problemService.findAll(PageRequest.of(20, 10));

        assertTrue(page.isEmpty());
        assertEquals(1, page.getTotalElements());
        assertEquals(0, aggregateQueryExecutions());
    }

    @Test
    void detailIncludesTheSameStatisticsAsTheList() {
        Problem problem = problem(true);
        submission(problem, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);
        submission(problem, 20L, SubmissionStatus.COMPLETED, SubmissionVerdict.WA);
        submission(problem, 20L, SubmissionStatus.COMPLETED, SubmissionVerdict.TLE);

        ProblemResponse detail = problemService.findById(problem.getId(), false);
        ProblemListResponse summary = problemService.findAll(PageRequest.of(0, 20))
                .getContent().getFirst();

        assertEquals(new BigDecimal("33.33"), detail.acceptanceRate());
        assertEquals(summary.acceptedSubmissionCount(), detail.acceptedSubmissionCount());
        assertEquals(summary.judgedSubmissionCount(), detail.judgedSubmissionCount());
        assertEquals(summary.acceptanceRate(), detail.acceptanceRate());
        assertNull(detail.spjSource());
    }

    @Test
    void solvedAndUnsolvedListsIncludeGlobalRatherThanPersonalStatistics() {
        Problem solved = problem(true);
        Problem attempted = problem(true);
        submission(solved, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);
        submission(solved, 20L, SubmissionStatus.COMPLETED, SubmissionVerdict.WA);
        submission(attempted, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.WA);
        submission(attempted, 20L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);

        ProblemListResponse solvedSummary = problemService
                .findSolvedByUserId(10L, PageRequest.of(0, 20)).getContent().getFirst();
        ProblemListResponse attemptedSummary = problemService
                .findUnsolvedByUserId(10L, PageRequest.of(0, 20)).getContent().getFirst();

        assertEquals(solved.getId(), solvedSummary.id());
        assertEquals(attempted.getId(), attemptedSummary.id());
        assertEquals(new BigDecimal("50.00"), solvedSummary.acceptanceRate());
        assertEquals(new BigDecimal("50.00"), attemptedSummary.acceptanceRate());
        assertEquals(2, solvedSummary.judgedSubmissionCount());
        assertEquals(2, attemptedSummary.judgedSubmissionCount());
    }

    @Test
    void hiddenProblemsStillRequireAdminAccess() {
        Problem hidden = problem(false);
        submission(hidden, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.AC);
        statistics.clear();

        assertThrows(ProblemNotFoundException.class,
                () -> problemService.findById(hidden.getId(), false, false));
        assertEquals(0, aggregateQueryExecutions());
        assertTrue(problemService.findAll(PageRequest.of(0, 20)).isEmpty());
        assertTrue(problemService.findSolvedByUserId(10L, PageRequest.of(0, 20)).isEmpty());

        ProblemResponse detail = problemService.findById(hidden.getId(), false, true);
        assertEquals(new BigDecimal("100.00"), detail.acceptanceRate());
        Page<ProblemListResponse> adminPage = problemService.findAll(
                PageRequest.of(0, 20), null, true);
        assertEquals(1, adminPage.getTotalElements());
        assertEquals(new BigDecimal("100.00"), adminPage.getContent().getFirst().acceptanceRate());
    }

    @Test
    void rejudgedAndFailedSubmissionsAreReflectedWithoutCounterUpdates() {
        Problem problem = problem(true);
        Submission submission = submission(problem, 10L, SubmissionStatus.COMPLETED, SubmissionVerdict.WA);
        assertEquals(new BigDecimal("0.00"), problemService.findById(problem.getId(), false).acceptanceRate());

        submission.setVerdict(SubmissionVerdict.AC);
        submissionRepository.saveAndFlush(submission);
        assertEquals(new BigDecimal("100.00"), problemService.findById(problem.getId(), false).acceptanceRate());

        submission.setStatus(SubmissionStatus.FAILED);
        submissionRepository.saveAndFlush(submission);
        ProblemResponse failed = problemService.findById(problem.getId(), false);
        assertEquals(0, failed.judgedSubmissionCount());
        assertEquals(0, failed.acceptedSubmissionCount());
        assertNull(failed.acceptanceRate());
    }

    private Problem problem(boolean enabled) {
        int number = ++problemNumber;
        Problem problem = new Problem("Problem " + number, "stats-problem-" + number,
                "A test problem", 1000, 256);
        problem.setEnabled(enabled);
        return problemRepository.saveAndFlush(problem);
    }

    private Submission submission(Problem problem, long userId,
            SubmissionStatus status, SubmissionVerdict verdict) {
        Submission submission = new Submission(problem.getId(), userId, "cpp", "int main() { return 0; }");
        submission.setStatus(status);
        submission.setVerdict(verdict);
        return submissionRepository.saveAndFlush(submission);
    }

    private long aggregateQueryExecutions() {
        return Arrays.stream(statistics.getQueries())
                .filter(query -> query.contains("new com.codeforge.backend.submission.dto.ProblemSubmissionStats"))
                .mapToLong(query -> statistics.getQueryStatistics(query).getExecutionCount())
                .sum();
    }
}
