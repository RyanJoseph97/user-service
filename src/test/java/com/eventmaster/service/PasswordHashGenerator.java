package com.eventmaster.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility test to generate BCrypt hashes for seed data.
 * Run this test to generate password hashes for schema.sql
 */
public class PasswordHashGenerator {

    @Test
    public void generatePasswordHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "password123";
        String hash = encoder.encode(password);

        System.out.println("Plain text password: " + password);
        System.out.println("BCrypt hash: " + hash);
        System.out.println();

        // Verify the hash works
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification test: " + (matches ? "PASS" : "FAIL"));

        // Test with your existing hash from schema.sql
        String existingHash = "$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing hash matches 'password123': " + existingMatches);
    }
}

