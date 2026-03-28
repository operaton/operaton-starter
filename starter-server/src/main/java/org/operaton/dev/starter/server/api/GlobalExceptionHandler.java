package org.operaton.dev.starter.server.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Translates all exceptions to RFC 7807 Problem Details responses.
 * No try/catch exists in the controller itself.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_URI = "https://start.operaton.org/problems/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String fields = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(BASE_URI + "validation-error"));
        problem.setTitle("Validation Error");
        problem.setDetail(fields);
        return ResponseEntity.badRequest()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/problem+json"))
                .body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex) {
        log.error("Unreadable request body", ex);
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(BASE_URI + "invalid-request"));
        problem.setTitle("Invalid Request");
        problem.setDetail("Request body could not be parsed. Check enum values and field types.");
        return ResponseEntity.badRequest()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/problem+json"))
                .body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(BASE_URI + "validation-error"));
        problem.setTitle("Validation Error");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.badRequest()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/problem+json"))
                .body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(BASE_URI + "invalid-request"));
        problem.setTitle("Invalid Request");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.badRequest()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/problem+json"))
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Unhandled server error", ex);
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(BASE_URI + "internal-error"));
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred. Please try again later.");
        return ResponseEntity.internalServerError()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/problem+json"))
                .body(problem);
    }
}
