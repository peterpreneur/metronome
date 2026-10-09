package com.peterpreneur.metronome.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps API exceptions to their HTTP status codes.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException exception) {
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ScheduleNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(ValidationException exception) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, exception);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, Exception exception) {
        return ResponseEntity.status(status)
                .body(Map.of("error", String.valueOf(exception.getMessage())));
    }
}
