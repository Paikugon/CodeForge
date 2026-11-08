package com.codeforge.backend.tag.service;

import com.codeforge.backend.tag.dto.TagRequest;
import com.codeforge.backend.tag.dto.TagResponse;
import com.codeforge.backend.tag.model.Tag;
import com.codeforge.backend.tag.repository.TagRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static com.codeforge.backend.common.error.ErrorCode.*;

@Service
public class TagService {
    private final TagRepository repository;

    public TagService(TagRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> findAll() {
        return repository.findAll(Sort.by("name", "id")).stream()
                .map(TagResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TagResponse findById(Long id) {
        return TagResponse.from(requireTag(id));
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        return save(new Tag(request.name().strip()));
    }

    @Transactional
    public TagResponse update(Long id, TagRequest request) {
        Tag tag = requireTag(id);
        tag.setName(request.name().strip());
        return save(tag);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = requireTag(id);
        repository.deleteAssignmentsByTagId(id);
        repository.delete(tag);
        repository.flush();
    }

    private Tag requireTag(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TAG_NOT_FOUND));
    }

    private TagResponse save(Tag tag) {
        return TagResponse.from(repository.saveAndFlush(tag));
    }
}
