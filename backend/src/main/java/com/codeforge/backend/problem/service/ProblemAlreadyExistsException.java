package com.codeforge.backend.problem.service;

public class ProblemAlreadyExistsException extends RuntimeException {

    public ProblemAlreadyExistsException(String slug) {
        super("A problem with slug '" + slug + "' already exists");
    }
}
