package com.eventmaster.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.eventmaster.exception.InvalidRefreshTokenException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across the service.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors - return 400 with a map of field → message
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));
        body.put("errors", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException - return 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalStateException - return 409
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        logger.warn("Conflict: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Object> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        logger.warn("Invalid refresh token: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle UserNotFoundException - return 404 with clear error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        logger.warn("User not found exception occurred: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "User Not Found");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle DuplicateUserException - return 409 Conflict when username or email is already taken
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Object> handleDuplicateUserException(DuplicateUserException ex) {
        logger.warn("Duplicate user exception occurred: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");

        String message;
        if (ex.isUsernameDuplicate()) {
            message = "The username is already taken. Please choose a different username.";
        } else if (ex.isEmailDuplicate()) {
            message = "The email address is already registered. Please use a different email.";
        } else {
            message = ex.getMessage();
        }

        body.put("message", message);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Fallback for unexpected errors - return 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex) {
        logger.error("Unexpected exception occurred", ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

