package com.codeforge.backend.tag.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    protected Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Tag tag
                && getId() != null
                && getId().equals(tag.getId());
    }

    @Override
    public final int hashCode() {
        // Keep Set membership stable before and after the generated ID is assigned.
        return Tag.class.hashCode();
    }
}
