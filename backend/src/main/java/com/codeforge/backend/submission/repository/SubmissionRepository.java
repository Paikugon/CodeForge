package com.codeforge.backend.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.codeforge.backend.submission.dto.SubmissionListResponse;
import com.codeforge.backend.submission.model.Submission;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {

    Page<Submission> findByProblemId(
        Long problemId,
        Pageable pageable
    );

    @Query("""
            select new com.codeforge.backend.submission.dto.SubmissionListResponse(
                s.id, s.problemId, s.language, s.status, s.verdict,
                s.executionTimeMs, s.memoryUsedKb, s.passedTestCases,
                s.totalTestCases, s.createdAt, s.updatedAt)
            from Submission s
            """)
    Page<SubmissionListResponse> findAllSummaries(Pageable pageable);

    @Query("""
            select new com.codeforge.backend.submission.dto.SubmissionListResponse(
                s.id, s.problemId, s.language, s.status, s.verdict,
                s.executionTimeMs, s.memoryUsedKb, s.passedTestCases,
                s.totalTestCases, s.createdAt, s.updatedAt)
            from Submission s
            where s.problemId = :problemId
            """)
    Page<SubmissionListResponse> findSummaryByProblemId(
            Long problemId,
            Pageable pageable);
}
