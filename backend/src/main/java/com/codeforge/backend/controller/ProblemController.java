package com.codeforge.backend.controller;

import com.codeforge.backend.dto.CreateProblemRequest;
import com.codeforge.backend.dto.ProblemResponse;
import com.codeforge.backend.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProblemResponse create(@Valid @RequestBody CreateProblemRequest request) {
        return problemService.create(request);
    }

    @GetMapping
    public List<ProblemResponse> findAll() {
        return problemService.findAll();
    }

    @GetMapping("/{id}")
    public ProblemResponse findById(@PathVariable Long id) {
        return problemService.findById(id);
    }
}
