package com.codeforge.backend.problem.repository;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.dto.ProblemListResponse;
import com.codeforge.backend.problem.enums.ProblemDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    boolean existsBySlug(String slug);

    @Query("select p.id from Problem p order by p.id")
    List<Long> findAllProblemIds();

    // Serialize test-case mutations for the same problem while assigning order indexes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Problem p where p.id = :id")
    Optional<Problem> findByIdForTestCaseUpdate(@Param("id") Long id);

    @Query("""
            select distinct p
            from Problem p
            left join fetch p.tags
            where p.id in :ids
            """)
    List<Problem> findAllWithTagsByIdIn(@Param("ids") Collection<Long> ids);

    @Query("""
            select new com.codeforge.backend.problem.dto.ProblemListResponse(
                p.id, p.title, p.slug, p.timeLimitMs, p.memoryLimitMb,
                p.difficulty, p.enabled, p.createdAt, p.updatedAt)
            from Problem p
            where (:enabled is null or p.enabled = :enabled)
              and (:difficulty is null or p.difficulty = :difficulty)
              and (:tagIds is null or (
                  select count(distinct tag.id) from p.tags tag where tag.id in :tagIds
              ) = :tagCount)
            """)
    Page<ProblemListResponse> findAllSummaries(
            @Param("enabled") Boolean enabled,
            @Param("difficulty") ProblemDifficulty difficulty,
            @Param("tagIds") Collection<Long> tagIds,
            @Param("tagCount") long tagCount,
            Pageable pageable);

    @Query(value = """
            select distinct new com.codeforge.backend.problem.dto.ProblemListResponse(
                p.id, p.title, p.slug, p.timeLimitMs, p.memoryLimitMb,
                p.difficulty, p.enabled, p.createdAt, p.updatedAt)
            from Problem p
            join com.codeforge.backend.submission.model.Submission s
                on s.problemId = p.id
            where s.userId = :userId
              and s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
              and (:enabled is null or p.enabled = :enabled)
              and (:difficulty is null or p.difficulty = :difficulty)
              and (:tagIds is null or (
                  select count(distinct tag.id) from p.tags tag where tag.id in :tagIds
              ) = :tagCount)
            """, countQuery = """
            select count(distinct p.id)
            from Problem p
            join com.codeforge.backend.submission.model.Submission s
                on s.problemId = p.id
            where s.userId = :userId
              and s.verdict = com.codeforge.backend.submission.enums.SubmissionVerdict.AC
              and (:enabled is null or p.enabled = :enabled)
              and (:difficulty is null or p.difficulty = :difficulty)
              and (:tagIds is null or (
                  select count(distinct tag.id) from p.tags tag where tag.id in :tagIds
              ) = :tagCount)
            """)
    Page<ProblemListResponse> findSolvedByUserId(
            @Param("userId") Long userId,
            @Param("enabled") Boolean enabled,
            @Param("difficulty") ProblemDifficulty difficulty,
            @Param("tagIds") Collection<Long> tagIds,
            @Param("tagCount") long tagCount,
            Pageable pageable);

    @Query(value = """
            select new com.codeforge.backend.problem.dto.ProblemListResponse(
                p.id, p.title, p.slug, p.timeLimitMs, p.memoryLimitMb,
                p.difficulty, p.enabled, p.createdAt, p.updatedAt)
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
            and (:enabled is null or p.enabled = :enabled)
            and (:difficulty is null or p.difficulty = :difficulty)
            and (:tagIds is null or (
                select count(distinct tag.id) from p.tags tag where tag.id in :tagIds
            ) = :tagCount)
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
            and (:enabled is null or p.enabled = :enabled)
            and (:difficulty is null or p.difficulty = :difficulty)
            and (:tagIds is null or (
                select count(distinct tag.id) from p.tags tag where tag.id in :tagIds
            ) = :tagCount)
            """)
    Page<ProblemListResponse> findUnsolvedByUserId(
            @Param("userId") Long userId,
            @Param("enabled") Boolean enabled,
            @Param("difficulty") ProblemDifficulty difficulty,
            @Param("tagIds") Collection<Long> tagIds,
            @Param("tagCount") long tagCount,
            Pageable pageable);
}
