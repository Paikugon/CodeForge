package com.codeforge.backend.repository;

import com.codeforge.backend.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TestCaseRepository
        extends JpaRepository<TestCase, Long> {

    List<TestCase> findByProblemIdOrderByOrderIndexAscIdAsc(
            Long problemId
    );

    Optional<TestCase> findByIdAndProblemId(
            Long id,
            Long problemId
    );
}