package com.codeforge.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "input", nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(name = "is_sample", nullable = false)
    private Boolean isSample = false;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected TestCase() {
    }

    public TestCase(
            Long problemId,
            String input,
            String expectedOutput,
            Boolean isSample,
            Integer orderIndex) {

        this.problemId = problemId;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.isSample = isSample;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public Boolean getIsSample() {
        return isSample;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}