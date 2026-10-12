package com.codeforge.backend.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeforge.backend.submission.model.Submission;

import java.util.List;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {

    List<Submission> findByProblemIdOrderByCreatedAtDesc(
        Long problemId
    );
}
