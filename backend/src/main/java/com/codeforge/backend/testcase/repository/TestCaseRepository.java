package com.codeforge.backend.testcase.repository;

import com.codeforge.backend.testcase.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import com.codeforge.backend.testcase.dto.TestCaseSummary;
import org.springframework.data.jpa.repository.Modifying;

public interface TestCaseRepository
		extends JpaRepository<TestCase, Long> {

	List<TestCase> findByProblemIdOrderByOrderIndexAscIdAsc(
			Long problemId);

	@Query("""
			select t
			from TestCase t
			where t.problemId = :problemId
			  and t.isSample = true
			order by t.orderIndex asc, t.id asc
			""")
	List<TestCase> findSampleByProblemId(@Param("problemId") Long problemId);

	Page<TestCase> findByProblemId(
			Long problemId,
			Pageable pageable);

	Optional<TestCase> findByIdAndProblemId(
			Long id,
			Long problemId);

	long countByProblemId(Long problemId);

    @Query("""
            select new com.codeforge.backend.testcase.dto.TestCaseSummary(
                t.id, t.isSample, t.orderIndex, length(t.input), length(t.expectedOutput), t.updatedAt)
            from TestCase t where t.problemId = :problemId
            """)
    Page<TestCaseSummary> findSummaries(@Param("problemId") Long problemId, Pageable pageable);

    @Query("select t.id from TestCase t where t.problemId = :problemId and t.id in :ids")
    List<Long> findSelectedIds(@Param("problemId") Long problemId, @Param("ids") List<Long> ids);

    @Modifying(flushAutomatically = true)
    @Query("update TestCase t set t.isSample = :sample, t.updatedAt = :updatedAt where t.problemId = :problemId and t.id in :ids")
    int updateSampleBatch(@Param("problemId") Long problemId, @Param("ids") List<Long> ids, @Param("sample") boolean sample,
            @Param("updatedAt") java.time.OffsetDateTime updatedAt);

    @Modifying(flushAutomatically = true)
    @Query("delete from TestCase t where t.problemId = :problemId and t.id in :ids")
    int deleteSelected(@Param("problemId") Long problemId, @Param("ids") List<Long> ids);

	@Query("select max(t.orderIndex) from TestCase t where t.problemId = :problemId")
	Integer findMaxOrderIndexByProblemId(@Param("problemId") Long problemId);
}
