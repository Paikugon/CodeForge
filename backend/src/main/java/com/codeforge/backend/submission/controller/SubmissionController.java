package com.codeforge.backend.submission.controller;

import com.codeforge.backend.common.pagination.PaginationValidator;
import com.codeforge.backend.submission.dto.SubmissionRequest;
import com.codeforge.backend.submission.dto.SubmissionResponse;
import com.codeforge.backend.submission.service.SubmissionService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse create(
            @Valid @RequestBody SubmissionRequest request) {
        return submissionService.create(request);
    }

    @GetMapping("/{id}")
    public SubmissionResponse getById(@PathVariable Long id) {
        return submissionService.getById(id);
    }

    @GetMapping
    public Page<SubmissionResponse> getAll(
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        PaginationValidator.validate(page, size, 100);

        return submissionService.getAll(problemId, pageable);
    }
}
