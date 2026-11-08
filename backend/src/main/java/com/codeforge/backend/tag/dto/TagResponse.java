package com.codeforge.backend.tag.dto;

import com.codeforge.backend.tag.model.Tag;

public record TagResponse(Long id, String name) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
