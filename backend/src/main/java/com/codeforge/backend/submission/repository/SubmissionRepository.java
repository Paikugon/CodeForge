package com.codeforge.backend.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.codeforge.backend.submission.model.Submission;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {

    Page<Submission> findByProblemId(
        Long problemId,
        Pageable pageable
    );
}
