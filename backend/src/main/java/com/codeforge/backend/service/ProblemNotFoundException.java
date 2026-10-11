package com.codeforge.backend.service;

public class ProblemNotFoundException extends RuntimeException {

    public ProblemNotFoundException(Long id) {
        super("Problem with id " + id + " was not found");
    }
}
