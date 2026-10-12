package com.codeforge.backend.submission.model;

import com.codeforge.backend.submission.enums.SubmissionStatus;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(nullable = false, length = 30)
    private String language;

    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubmissionVerdict verdict;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "memory_used_kb")
    private Integer memoryUsedKb;

    @Column(name = "passed_test_cases", nullable = false)
    private Integer passedTestCases = 0;

    @Column(name = "total_test_cases", nullable = false)
    private Integer totalTestCases = 0;

    @Column(name = "compile_output", columnDefinition = "TEXT")
    private String compileOutput;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Submission() {
    }

    public Submission(Long problemId, String language, String sourceCode) {
        this.problemId = problemId;
        this.language = language;
        this.sourceCode = sourceCode;
    }

    public Long getId() {
        return id;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getLanguage() {
        return language;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public SubmissionVerdict getVerdict() {
        return verdict;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Integer getMemoryUsedKb() {
        return memoryUsedKb;
    }

    public Integer getPassedTestCases() {
        return passedTestCases;
    }

    public Integer getTotalTestCases() {
        return totalTestCases;
    }

    public String getCompileOutput() {
        return compileOutput;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
