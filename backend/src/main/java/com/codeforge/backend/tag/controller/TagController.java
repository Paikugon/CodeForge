package com.codeforge.backend.tag.controller;

import com.codeforge.backend.tag.dto.TagRequest;
import com.codeforge.backend.tag.dto.TagResponse;
import com.codeforge.backend.tag.service.TagService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService service;

    public TagController(TagService service) {
        this.service = service;
    }

    @GetMapping
    public List<TagResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TagResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        TagResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/tags/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TagResponse update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
