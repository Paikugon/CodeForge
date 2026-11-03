package com.codeforge.backend.problem.service;

public class ProblemAlreadyExistsException extends RuntimeException {

    public ProblemAlreadyExistsException(String slug) {
        super("CODE_PROBLEM_ALREADY_EXISTS");
    }
}
