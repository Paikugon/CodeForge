package com.codeforge.backend.repository;

import com.codeforge.backend.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    boolean existsBySlug(String slug);
}
