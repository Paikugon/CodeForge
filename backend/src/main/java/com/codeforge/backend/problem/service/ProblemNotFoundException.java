package com.codeforge.backend.problem.service;

public class ProblemNotFoundException extends RuntimeException {

    public ProblemNotFoundException(Long id) {
        super("CODE_PROBLEM_NOT_FOUND");
    }
}
