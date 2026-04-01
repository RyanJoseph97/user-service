package com.eventmaster.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    public void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    public void handleIllegalArgument_returns400WithMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad input value");

        ResponseEntity<Object> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Bad input value", body.get("message"));
    }

    @Test
    public void handleIllegalState_returns409WithMessage() {
        IllegalStateException ex = new IllegalStateException("Already following bob");

        ResponseEntity<Object> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertEquals("Conflict", body.get("error"));
        assertEquals("Already following bob", body.get("message"));
    }

    @Test
    public void handleUserNotFound_returns404WithMessage() {
        UserNotFoundException ex = UserNotFoundException.byUsername("ghost");

        ResponseEntity<Object> response = handler.handleUserNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("User Not Found", body.get("error"));
        assertTrue(body.get("message").toString().contains("ghost"));
    }

    @Test
    public void handleDuplicateUser_usernameDuplicate_returns409() {
        DuplicateUserException ex = new DuplicateUserException("username", "takenuser");

        ResponseEntity<Object> response = handler.handleDuplicateUserException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertTrue(body.get("message").toString().contains("username"));
    }

    @Test
    public void handleDuplicateUser_emailDuplicate_returns409() {
        DuplicateUserException ex = new DuplicateUserException("email", "taken@example.com");

        ResponseEntity<Object> response = handler.handleDuplicateUserException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertTrue(body.get("message").toString().contains("email"));
    }

    @Test
    public void handleGlobalException_returns500() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<Object> response = handler.handleGlobalException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
    }
}
