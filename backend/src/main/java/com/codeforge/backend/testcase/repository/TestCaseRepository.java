package com.codeforge.backend.testcase.repository;

import com.codeforge.backend.testcase.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface TestCaseRepository
        extends JpaRepository<TestCase, Long> {

    List<TestCase> findByProblemIdOrderByOrderIndexAscIdAsc(
            Long problemId
    );

    Page<TestCase> findByProblemId(
            Long problemId,
            Pageable pageable
    );

    Optional<TestCase> findByIdAndProblemId(
            Long id,
            Long problemId
    );

    long countByProblemId(Long problemId);
}