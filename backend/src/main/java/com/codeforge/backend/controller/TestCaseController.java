package com.codeforge.backend.controller;

import com.codeforge.backend.dto.TestCaseRequest;
import com.codeforge.backend.dto.TestCaseResponse;
import com.codeforge.backend.service.TestCaseService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems/{problemId}/test-cases")
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
    public List<TestCaseResponse> findAll(
            @PathVariable Long problemId) {

        return testCaseService.findAll(problemId);
    }
}