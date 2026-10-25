package com.codeforge.backend.problem.repository;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.dto.ProblemListResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    boolean existsBySlug(String slug);

    @Query("""
            select new com.codeforge.backend.problem.dto.ProblemListResponse(
                p.id, p.title, p.slug, p.timeLimitMs, p.memoryLimitMb,
                p.difficulty, p.createdAt, p.updatedAt)
            from Problem p
            """)
    Page<ProblemListResponse> findAllSummaries(Pageable pageable);
}
