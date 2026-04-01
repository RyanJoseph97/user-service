package com.eventmaster.service;

import com.eventmaster.jwt.JwtConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test JWT token generation and validation using the shared jwt-common module.
 */
public class JwtTest {

    // Must be >= 32 characters to satisfy HMAC-SHA256 key length requirement.
    private static final String TEST_SECRET = "test-secret-key-minimum-32-chars!!";

    @Test
    public void testJwtTokenGenerationAndValidation() {
        JwtConfig jwtConfig = new JwtConfig(TEST_SECRET);
        String username = "testuser";

        String token = jwtConfig.generateToken(username);
        System.out.println("Generated JWT token: " + token);

        String extractedUsername = jwtConfig.extractUsername(token);
        assertEquals(username, extractedUsername, "Extracted username should match original");

        assertTrue(jwtConfig.validateToken(token), "Token should be valid");

        System.out.println("JWT token generation and validation tests passed!");
    }

    @Test
    public void testTamperedTokenIsInvalid() {
        JwtConfig jwtConfig = new JwtConfig(TEST_SECRET);
        String token = jwtConfig.generateToken("testuser");

        String tamperedToken = token.substring(0, token.length() - 4) + "xxxx";
        assertFalse(jwtConfig.validateToken(tamperedToken), "Tampered token should be invalid");
    }

    @Test
    public void testTokenSignedWithDifferentSecretIsInvalid() {
        JwtConfig issuer = new JwtConfig(TEST_SECRET);
        JwtConfig verifier = new JwtConfig("completely-different-secret-key-here!!");

        String token = issuer.generateToken("testuser");
        assertFalse(verifier.validateToken(token), "Token signed with a different secret should be rejected");
    }
}
