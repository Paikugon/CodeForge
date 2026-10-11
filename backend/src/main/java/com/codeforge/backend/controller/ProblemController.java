package com.codeforge.backend.controller;

import com.codeforge.backend.dto.CreateProblemRequest;
import com.codeforge.backend.dto.ProblemResponse;
import com.codeforge.backend.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProblemResponse create(@Valid @RequestBody CreateProblemRequest request) {
        return problemService.create(request);
    }

    @GetMapping
    public Page<ProblemResponse> findAll(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        validatePagination(page, size);

        return problemService.findAll(pageable);
    }

    private void validatePagination(String page, String size) {
        if (page != null) {
            try {
                if (Integer.parseInt(page) < 0) {
                    throw new IllegalArgumentException(
                            "Page must be greater than or equal to 0");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Page must be a valid integer");
            }
        }

        if (size != null) {
            try {
                if (Integer.parseInt(size) <= 0) {
                    throw new IllegalArgumentException(
                            "Size must be greater than 0");
                }
                if (Integer.parseInt(size) > 100) {
                    throw new IllegalArgumentException(
                            "Size must be less than or equal to 100");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Size must be a valid integer");
            }
        }
    }

    @GetMapping("/{id}")
    public ProblemResponse findById(@PathVariable Long id) {
        return problemService.findById(id);
    }
}
