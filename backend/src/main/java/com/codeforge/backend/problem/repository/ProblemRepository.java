package com.codeforge.backend.problem.repository;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.dto.ProblemListResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    boolean existsBySlug(String slug);

    @Query("""
            select new com.codeforge.backend.problem.dto.ProblemListResponse(
                p.id, p.title, p.slug, p.timeLimitMs, p.memoryLimitMb,
                p.difficulty, p.createdAt, p.updatedAt)
            from Problem p
            """)
    Page<ProblemListResponse> findAllSummaries(Pageable pageable);

    @Query(value = """
            select distinct new com.codeforge.backend.problem.dto.ProblemListResponse(
                p.id, p.title, p.slug, p.timeLimitMs, p.memoryLimitMb,
                p.difficulty, p.createdAt, p.updatedAt)
            from Problem p
            join com.codeforge.backend.submission.model.Submission s
                on s.problemId = p.id
            where s.userId = :userId
              and s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
            """, countQuery = """
            select count(distinct p.id)
            from Problem p
            join com.codeforge.backend.submission.model.Submission s
                on s.problemId = p.id
            where s.userId = :userId
              and s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
            """)
    Page<ProblemListResponse> findSolvedByUserId(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query(value = """
            select new com.codeforge.backend.problem.dto.ProblemListResponse(
                p.id, p.title, p.slug, p.timeLimitMs, p.memoryLimitMb,
                p.difficulty, p.createdAt, p.updatedAt)
            from Problem p
            where exists (
                select 1
                from Submission s
                where s.problemId = p.id
                  and s.userId = :userId
                  and s.verdict is not null
            )
            and not exists (
                select 1
                from Submission s
                where s.problemId = p.id
                  and s.userId = :userId
                  and s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
            )
            """, countQuery = """
            select count(p.id)
            from Problem p
            where exists (
                select 1
                from Submission s
                where s.problemId = p.id
                  and s.userId = :userId
                  and s.verdict is not null
            )
            and not exists (
                select 1
                from Submission s
                where s.problemId = p.id
                  and s.userId = :userId
                  and s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
            )
            """)
    Page<ProblemListResponse> findUnsolvedByUserId(
            @Param("userId") Long userId,
            Pageable pageable);
}
