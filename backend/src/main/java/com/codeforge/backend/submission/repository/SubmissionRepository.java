package com.codeforge.backend.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeforge.backend.submission.dto.SubmissionListResponse;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import com.codeforge.backend.submission.model.Submission;

public interface SubmissionRepository
		extends JpaRepository<Submission, Long> {

	Page<Submission> findByProblemId(
			Long problemId,
			Pageable pageable);

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
			  and not exists (
			      select 1
			      from Submission s2
			      where s2.problemId = s.problemId
			        and s2.userId = :userId
			        and s2.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
			  )
			""")
	long countUnsolvedProblemsByUserId(@Param("userId") Long userId);
}
