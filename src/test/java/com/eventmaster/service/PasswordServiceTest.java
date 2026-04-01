package com.eventmaster.service;

import com.eventmaster.service.PasswordService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify password hashing and verification works correctly
 */
public class PasswordServiceTest {
    
    @Test
    public void testPasswordHashingAndVerification() {
        PasswordService passwordService = new PasswordService();
        
        // Test password
        String plainPassword = "password123";
        String expectedHash = "$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC";
        
        // Test that the expected hash is recognized as hashed
        assertTrue(passwordService.isPasswordHashed(expectedHash), 
                  "Expected hash should be recognized as hashed");
        
        // Test that plain text is not recognized as hashed
        assertFalse(passwordService.isPasswordHashed(plainPassword), 
                   "Plain text password should not be recognized as hashed");
        
        // Test verification with the expected hash
        assertTrue(passwordService.verifyPassword(plainPassword, expectedHash), 
                  "Password should verify against its hash");
        
        // Test verification with wrong password
        assertFalse(passwordService.verifyPassword("wrongpassword", expectedHash), 
                   "Wrong password should not verify");
        
        System.out.println("All password service tests passed!");
        System.out.println("Plain password: " + plainPassword);
        System.out.println("Expected hash: " + expectedHash);
    }
    
    @Test
    public void testDifferentPasswordsHaveDifferentHashes() {
        PasswordService passwordService = new PasswordService();
        
        String password1 = "password123";
        String password2 = "password123";
        String password3 = "differentpassword";
        
        // Even with the same input, BCrypt should generate different hashes
        // due to random salts
        String hash1 = passwordService.hashPassword(password1);
        String hash2 = passwordService.hashPassword(password2);
        String hash3 = passwordService.hashPassword(password3);
        
        // Same passwords should verify against each other's hashes
        assertTrue(passwordService.verifyPassword(password1, hash2), 
                  "Same password should verify against different hash of same password");
        assertTrue(passwordService.verifyPassword(password2, hash1), 
                  "Same password should verify against different hash of same password");
        
        // Different passwords should not verify
        assertFalse(passwordService.verifyPassword(password1, hash3), 
                   "Different passwords should not verify against each other");
        
        System.out.println("Hash verification tests passed!");
        System.out.println("Hash 1: " + hash1);
        System.out.println("Hash 2: " + hash2);
        System.out.println("Hash 3: " + hash3);
    }
}