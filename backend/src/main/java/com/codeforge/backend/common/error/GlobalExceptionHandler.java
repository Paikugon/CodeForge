package com.codeforge.backend.common.error;

import com.codeforge.backend.common.dto.ApiErrorResponse;
import com.codeforge.backend.problem.service.ProblemAlreadyExistsException;
import com.codeforge.backend.problem.service.ProblemNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeforge.backend.common.error.ErrorCode.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        log.error(
                "Illegal argument at {}",
                request.getRequestURI(),
                exception);
        return error(
                HttpStatus.BAD_REQUEST,
                responseCode(exception.getMessage(), INVALID_VALUE),
                request);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException exception,
            HttpServletRequest request) {
        log.error(
                "Authorization denied at {}",
                request.getRequestURI(),
                exception);
        return error(
                HttpStatus.FORBIDDEN,
                FORBIDDEN,
                request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSort(
            PropertyReferenceException exception,
            HttpServletRequest request) {
        log.error(
                "Invalid sort property at {}",
                request.getRequestURI(),
                exception);
        return error(
                HttpStatus.BAD_REQUEST,
                INVALID_SORT_PROPERTY,
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        log.error(
                "Type mismatch at {}",
                request.getRequestURI(),
                exception);
        return error(
                HttpStatus.BAD_REQUEST,
                INVALID_VALUE,
                request);
    }

    @ExceptionHandler(ProblemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProblemNotFound(
            ProblemNotFoundException exception,
            HttpServletRequest request) {
        log.error(
                "Problem not found at {}",
                request.getRequestURI(),
                exception);
        return error(HttpStatus.NOT_FOUND, PROBLEM_NOT_FOUND, request);
    }

    @ExceptionHandler(ProblemAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleProblemAlreadyExists(
            ProblemAlreadyExistsException exception,
            HttpServletRequest request) {
        log.error(
                "Problem already exists at {}",
                request.getRequestURI(),
                exception);
        return error(HttpStatus.CONFLICT, PROBLEM_ALREADY_EXISTS, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        log.error(
                "Validation failed at {}",
                request.getRequestURI(),
                exception);
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(fieldError -> fieldErrors.putIfAbsent(
                        fieldError.getField(),
                        validationCode(fieldError)));

        return error(HttpStatus.BAD_REQUEST, REQUEST_VALIDATION_FAILED, request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        log.error(
                "Constraint violation at {}",
                request.getRequestURI(),
                exception);
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            int lastPathSeparator = field.lastIndexOf('.');
            if (lastPathSeparator >= 0) {
                field = field.substring(lastPathSeparator + 1);
            }

            fieldErrors.putIfAbsent(
                    field,
                    validationCode(field, violation.getConstraintDescriptor()
                            .getAnnotation()
                            .annotationType()
                            .getSimpleName()));
        });

        return error(
                HttpStatus.BAD_REQUEST,
                REQUEST_VALIDATION_FAILED,
                request,
                fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        log.error(
                "Malformed request at {}",
                request.getRequestURI(),
                exception);
        return error(HttpStatus.BAD_REQUEST, INVALID_REQUEST_BODY, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request) {
        log.error(
                "Response status error at {}",
                request.getRequestURI(),
                exception);
        return error(
                HttpStatus.valueOf(exception.getStatusCode().value()),
                responseCode(exception.getReason(), INVALID_VALUE),
                request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.UNAUTHORIZED,
                INVALID_EMAIL_OR_PASSWORD,
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {

        log.error(
                "Unexpected error at {}",
                request.getRequestURI(),
                exception);

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                UNEXPECTED_ERROR,
                request);
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        return error(status, message, request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors);
        return ResponseEntity.status(status).body(response);
    }

    private String responseCode(String message, String fallback) {
        return message != null && message.startsWith("CODE_")
                ? message
                : fallback;
    }

    private String validationCode(FieldError fieldError) {
        String constraint = fieldError.getCode();

        return validationCode(
                fieldError.getField(),
                constraint == null ? "" : constraint);
    }

    private String validationCode(String field, String constraint) {
        String fieldCode = field.replaceAll("([a-z])([A-Z])", "$1_$2")
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);

        if (constraint.contains("NotBlank") || constraint.contains("NotNull")) {
            return "CODE_" + fieldCode + "_REQUIRED";
        }
        if (constraint.contains("Email")) {
            return "CODE_" + fieldCode + "_INVALID_EMAIL";
        }
        if (constraint.contains("Pattern")) {
            return "CODE_" + fieldCode + "_INVALID_FORMAT";
        }
        if (constraint.contains("Size")) {
            return "CODE_" + fieldCode + "_INVALID_LENGTH";
        }
        if (constraint.contains("Positive")
                || constraint.contains("Min")
                || constraint.contains("Max")) {
            return "CODE_" + fieldCode + "_INVALID_VALUE";
        }
        return "CODE_" + fieldCode + "_INVALID";
    }
}
