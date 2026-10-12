package com.codeforge.backend.submission.controller;

import com.codeforge.backend.submission.dto.SubmissionRequest;
import com.codeforge.backend.submission.dto.SubmissionResponse;
import com.codeforge.backend.submission.service.SubmissionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<SubmissionResponse> getAll(
            @RequestParam(required = false) Long problemId) {
        return submissionService.getAll(problemId);
    }
}
