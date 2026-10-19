package com.codeforge.backend.problem.controller;

import com.codeforge.backend.common.pagination.PaginationValidator;
import com.codeforge.backend.problem.dto.ProblemRequest;
import com.codeforge.backend.problem.dto.ProblemListResponse;
import com.codeforge.backend.problem.dto.ProblemResponse;
import com.codeforge.backend.problem.dto.RunCodeRequest;
import com.codeforge.backend.problem.dto.RunCodeResponse;
import com.codeforge.backend.problem.service.ProblemService;
import com.codeforge.backend.problem.service.RunCodeService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;
    private final RunCodeService runCodeService;

    public ProblemController(ProblemService problemService, RunCodeService runCodeService) {
        this.problemService = problemService;
        this.runCodeService = runCodeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProblemResponse create(@Valid @RequestBody ProblemRequest request) {
        return problemService.create(request);
    }

    @GetMapping
    public Page<ProblemListResponse> findAll(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PaginationValidator.validate(page, size, 100);

        return problemService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ProblemResponse findById(@PathVariable Long id) {
        return problemService.findById(id);
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<RunCodeResponse> run(
            @PathVariable Long id,
            @Valid @RequestBody RunCodeRequest request) {

        return ResponseEntity.ok(
                runCodeService.run(id, request));
    }
}
