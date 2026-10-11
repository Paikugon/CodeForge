package com.codeforge.backend.service;

public class ProblemAlreadyExistsException extends RuntimeException {

    public ProblemAlreadyExistsException(String slug) {
        super("A problem with slug '" + slug + "' already exists");
    }
}
