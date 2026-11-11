package com.codeforge.backend.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.codeforge.backend.submission.dto.SubmissionListResponse;
import com.codeforge.backend.submission.dto.ProblemSubmissionStats;
import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import com.codeforge.backend.submission.model.Submission;

public interface SubmissionRepository
		extends JpaRepository<Submission, Long> {

	Page<Submission> findByProblemId(
			Long problemId,
			Pageable pageable);

	@Transactional(readOnly = true)
	boolean existsByProblemId(long problemId);

	@Query("""
			select new com.codeforge.backend.submission.dto.SubmissionListResponse(
			    s.id, s.userId, u.username, s.problemId, s.language, s.status, s.verdict,
			    s.executionTimeMs, s.memoryUsedKb, s.passedTestCases,
			    s.totalTestCases, s.createdAt, s.updatedAt)
			from Submission s
			left join User u on u.id = s.userId
			""")
	Page<SubmissionListResponse> findAllSummaries(Pageable pageable);

	@Query("""
			select new com.codeforge.backend.submission.dto.SubmissionListResponse(
			    s.id, s.userId, u.username, s.problemId, s.language, s.status, s.verdict,
			    s.executionTimeMs, s.memoryUsedKb, s.passedTestCases,
			    s.totalTestCases, s.createdAt, s.updatedAt)
			from Submission s
			left join User u on u.id = s.userId
			where s.problemId = :problemId
			""")
	Page<SubmissionListResponse> findSummaryByProblemId(
			Long problemId,
			Pageable pageable);

	@Query("""
			select new com.codeforge.backend.submission.dto.SubmissionListResponse(
			    s.id, s.userId, u.username, s.problemId, s.language, s.status, s.verdict,
			    s.executionTimeMs, s.memoryUsedKb, s.passedTestCases,
			    s.totalTestCases, s.createdAt, s.updatedAt)
			from Submission s
			left join User u on u.id = s.userId
			where s.userId = :userId
			  and (:problemId is null or s.problemId = :problemId)
			  and (:verdict is null or s.verdict = :verdict)
			""")
	Page<SubmissionListResponse> findSummariesByUserId(
			@Param("userId") Long userId,
			@Param("problemId") Long problemId,
			@Param("verdict") SubmissionVerdict verdict,
			Pageable pageable);

	@Query("""
            select new com.codeforge.backend.submission.dto.ProblemSubmissionStats(
                s.problemId,
                count(s),
                sum(case when s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
                         then 1 else 0 end))
            from Submission s
            where s.problemId in :problemIds
              and s.status = com.codeforge.backend.submission.enums.SubmissionStatus.COMPLETED
              and s.verdict in (
                  com.codeforge.backend.submission.enums.SubmissionVerdict.AC,
                  com.codeforge.backend.submission.enums.SubmissionVerdict.WA,
                  com.codeforge.backend.submission.enums.SubmissionVerdict.TLE,
                  com.codeforge.backend.submission.enums.SubmissionVerdict.MLE,
                  com.codeforge.backend.submission.enums.SubmissionVerdict.RE,
                  com.codeforge.backend.submission.enums.SubmissionVerdict.CE)
            group by s.problemId
            """)
    List<ProblemSubmissionStats> findProblemSubmissionStats(
            @Param("problemIds") Collection<Long> problemIds);

	long countByUserId(Long userId);

	@Query("""
			select count(s)
			from Submission s
			where s.userId = :userId
			  and s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
			""")
	long countAcceptedByUserId(@Param("userId") Long userId);

	@Query("""
			select count(distinct s.problemId)
			from Submission s
			where s.userId = :userId
			  and s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
			""")
	long countSolvedProblemsByUserId(@Param("userId") Long userId);

	@Query("""
			select count(distinct s.problemId)
			from Submission s
			where s.userId = :userId
			  and s.verdict is not null
			  and not exists (
			      select 1
			      from Submission s2
			      where s2.problemId = s.problemId
			        and s2.userId = :userId
			        and s2.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
			  )
			""")
	long countUnsolvedProblemsByUserId(@Param("userId") Long userId);

	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update Submission s
			set s.status = :failedStatus,
			    s.errorMessage = :errorMessage,
			    s.updatedAt = :updatedAt
			where s.status in :unfinishedStatuses
			""")
	int failUnfinishedSubmissions(
			@Param("unfinishedStatuses") Collection<SubmissionStatus> unfinishedStatuses,
			@Param("failedStatus") SubmissionStatus failedStatus,
			@Param("errorMessage") String errorMessage,
			@Param("updatedAt") OffsetDateTime updatedAt);
}
