package com.codeforge.backend.testcase.controller;

import com.codeforge.backend.common.pagination.PaginationValidator;
import com.codeforge.backend.testcase.dto.TestCaseRequest;
import com.codeforge.backend.testcase.dto.TestCaseResponse;
import com.codeforge.backend.testcase.service.TestCaseService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems/{problemId}/test-cases")
@PreAuthorize("hasRole('ADMIN')")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @PostMapping
    public ResponseEntity<TestCaseResponse> create(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseRequest request) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(testCaseService.create(problemId, request));
    }

    @GetMapping
    public Page<TestCaseResponse> findAll(
            @PathVariable Long problemId,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @PageableDefault(
                    size = 100,
                    sort = {"orderIndex", "id"},
                    direction = Sort.Direction.ASC) Pageable pageable) {
        PaginationValidator.validate(page, size, 500);

        return testCaseService.findAll(problemId, pageable);
    }

    @PutMapping("/{testCaseId}")
    public TestCaseResponse update(
            @PathVariable Long problemId,
            @PathVariable Long testCaseId,
            @Valid @RequestBody TestCaseRequest request) {

        return testCaseService.update(problemId, testCaseId, request);
    }

    @DeleteMapping("/{testCaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long problemId,
            @PathVariable Long testCaseId) {

        testCaseService.delete(problemId, testCaseId);
    }
}