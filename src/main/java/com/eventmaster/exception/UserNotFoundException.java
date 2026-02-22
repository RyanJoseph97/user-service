package com.eventmaster.exception;

/**
 * Thrown when a User is not found in the database.
 * This is a recoverable application error (not a programming error).
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct exception for user not found by username
     */
    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("User not found with username: " + username);
    }

    /**
     * Construct exception for user not found by email
     */
    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("User not found with email: " + email);
    }

    /**
     * Construct exception for user not found by ID
     */
    public static UserNotFoundException byId(Long id) {
        return new UserNotFoundException("User not found with id: " + id);
    }
}

