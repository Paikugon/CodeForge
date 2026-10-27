package com.codeforge.backend.submission.controller;

import com.codeforge.backend.common.pagination.PaginationValidator;
import com.codeforge.backend.submission.dto.SubmissionRequest;
import com.codeforge.backend.submission.dto.SubmissionListResponse;
import com.codeforge.backend.submission.dto.SubmissionResponse;
import com.codeforge.backend.submission.enums.SubmissionVerdict;
import com.codeforge.backend.submission.service.SubmissionService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

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
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        long userId = Long.parseLong(jwt.getSubject());
        return submissionService.create(request, userId);
    }

    @GetMapping("/{id}")
    public SubmissionResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        long userId = (jwt == null) ? -1L : Long.parseLong(jwt.getSubject());

        return submissionService.getById(id, userId);
    }

    @GetMapping
    public Page<SubmissionListResponse> getAll(
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PaginationValidator.validate(page, size, 100);

        return submissionService.getAll(problemId, pageable);
    }

    @GetMapping("/me")
    public Page<SubmissionListResponse> getMine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) SubmissionVerdict verdict,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PaginationValidator.validate(page, size, 100);

        long userId = Long.parseLong(jwt.getSubject());

        return submissionService.getMine(userId, problemId, verdict, pageable);
    }
}
