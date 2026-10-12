package com.codeforge.backend.service;

import com.codeforge.backend.dto.ProblemRequest;
import com.codeforge.backend.dto.ProblemResponse;
import com.codeforge.backend.entity.Problem;
import com.codeforge.backend.repository.ProblemRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional
    public ProblemResponse create(ProblemRequest request) {
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
            if (cause instanceof ConstraintViolationException constraintEx
                    && "uk_problems_slug".equals(
                            constraintEx.getConstraintName())) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }

    @Transactional(readOnly = true)
    public Page<ProblemResponse> findAll(Pageable pageable) {
        return problemRepository.findAll(pageable)
                .map(ProblemResponse::from);
    }

    @Transactional(readOnly = true)
    public ProblemResponse findById(Long id) {
        return problemRepository.findById(id)
                .map(ProblemResponse::from)
                .orElseThrow(() -> new ProblemNotFoundException(id));
    }

}
