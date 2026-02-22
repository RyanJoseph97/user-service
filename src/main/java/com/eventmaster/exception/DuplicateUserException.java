package com.eventmaster.exception;

/**
 * Thrown when attempting to create a user with a duplicate username or email.
 * This indicates a business rule violation - emails and usernames must be unique.
 */
public class DuplicateUserException extends RuntimeException {

    private final String field; // "username" or "email"

    public DuplicateUserException(String field, String value) {
        super(String.format("%s '%s' is already in use", field, value));
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public boolean isUsernameDuplicate() {
        return "username".equalsIgnoreCase(field);
    }

    public boolean isEmailDuplicate() {
        return "email".equalsIgnoreCase(field);
    }
}

