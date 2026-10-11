package com.codeforge.backend.service;

import com.codeforge.backend.dto.CreateProblemRequest;
import com.codeforge.backend.dto.ProblemResponse;
import com.codeforge.backend.entity.Problem;
import com.codeforge.backend.repository.ProblemRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional
    public ProblemResponse create(CreateProblemRequest request) {
        if (problemRepository.existsBySlug(request.slug())) {
            throw new ProblemAlreadyExistsException(request.slug());
        }

        Problem problem = new Problem(
                request.title(),
                request.slug(),
                request.description(),
                request.timeLimitMs() == null ? 1000 : request.timeLimitMs(),
                request.memoryLimitMb() == null ? 256 : request.memoryLimitMb());

        try {
            return ProblemResponse.from(problemRepository.saveAndFlush(problem));
        } catch (DataIntegrityViolationException ex) {
            if (isSlugConstraintViolation(ex)) {
                throw new ProblemAlreadyExistsException(request.slug());
            }

            throw ex;
        }
    }

    private boolean isSlugConstraintViolation(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintEx) {
                return "uk_problems_slug".equals(
                        constraintEx.getConstraintName());
            }

            cause = cause.getCause();
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<ProblemResponse> findAll() {
        return problemRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(ProblemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProblemResponse findById(Long id) {
        return problemRepository.findById(id)
                .map(ProblemResponse::from)
                .orElseThrow(() -> new ProblemNotFoundException(id));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ProblemNotFoundException extends RuntimeException {

        public ProblemNotFoundException(Long id) {
            super("Problem with id " + id + " was not found");
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ProblemAlreadyExistsException extends RuntimeException {

        public ProblemAlreadyExistsException(String slug) {
            super("A problem with slug '" + slug + "' already exists");
        }
    }
}
