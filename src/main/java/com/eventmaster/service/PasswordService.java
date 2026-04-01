package com.eventmaster.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for handling password encryption and validation using BCrypt.
 * This ensures passwords are never stored in plain text.
 */
@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Hash a plain text password using BCrypt.
     * 
     * @param rawPassword the plain text password to hash
     * @return the hashed password
     */
    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verify a plain text password against a hashed password.
     * 
     * @param rawPassword the plain text password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if the password matches, false otherwise
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Check if a password is already hashed (starts with BCrypt prefix).
     * 
     * @param password the password to check
     * @return true if the password appears to be hashed, false otherwise
     */
    public boolean isPasswordHashed(String password) {
        if (password == null) {
            return false;
        }
        // BCrypt hashes start with $2a$, $2b$, or $2y$
        return password.startsWith("$2a$") || 
               password.startsWith("$2b$") || 
               password.startsWith("$2y$");
    }
}