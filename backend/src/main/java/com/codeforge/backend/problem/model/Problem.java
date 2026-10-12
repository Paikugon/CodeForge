package com.codeforge.backend.problem.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "time_limit_ms", nullable = false)
    private Integer timeLimitMs = 1000;

    @Column(name = "memory_limit_mb", nullable = false)
    private Integer memoryLimitMb = 256;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Generated(event = EventType.INSERT)
    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;

    protected Problem() {
    }

    public Problem(
            String title,
            String slug,
            String description,
            Integer timeLimitMs,
            Integer memoryLimitMb) {
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public Integer getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}