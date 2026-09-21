package com.identity.api.exception;

import com.identity.core.exception.DuplicateEntryException;
import com.identity.core.exception.InvalidCredentialsException;
import com.identity.core.exception.InvalidTokenException;
import com.identity.core.exception.ResourceNotFoundException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException exception) {
        logger.warn("Resource not found: {}", exception.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage());
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ProblemDetail handleDuplicate(DuplicateEntryException exception) {
        logger.warn("Duplicate entry attempt: {}", exception.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate entry", exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
        logger.warn("Invalid login attempt");
        return problem(HttpStatus.UNAUTHORIZED, "Authentication failed", exception.getMessage());
    }

    @ExceptionHandler({InvalidTokenException.class, AuthenticationCredentialsNotFoundException.class})
    public ProblemDetail handleInvalidToken(RuntimeException exception) {
        logger.warn("Invalid token: {}", exception.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, "Authentication failed", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        logger.warn("Access denied: {}", exception.getMessage());
        return problem(HttpStatus.FORBIDDEN, "Access denied", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((left, right) -> left + "; " + right)
                .orElse("Validation failed");
        logger.warn("Request validation failed: {}", detail);
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception exception) {
        logger.error("Unhandled application error", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
