package com.eventmaster.controller;

import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.User;
import com.eventmaster.model.LoginResponse;
import com.eventmaster.service.UserService;
import com.eventmaster.service.PasswordService;
import com.eventmaster.jwt.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final PasswordService passwordService;

    @Autowired
    public UserController(UserService userService, PasswordService passwordService, JwtConfig jwtConfig) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtConfig = jwtConfig;
    }

    private final JwtConfig jwtConfig;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        logger.debug("GET request received for user id: {}", id);
        return userService.findById(id)
                .map(user -> {
                    logger.info("Returning user with id: {}", id);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    logger.warn("User not found for id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username){
        logger.debug("GET request received for username: {}", username);
        User user = userService.findByUsername(username);
        if (user != null){
            logger.info("Returning user for username: {}", username);
            return ResponseEntity.ok(user);
        } else {
            logger.warn("User not found for username: {}", username);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email){
        logger.debug("GET request received for email: {}", email);
        User user = userService.findByEmail(email);
        if (user != null){
            logger.info("Returning user for email: {}", email);
            return ResponseEntity.ok(user);
        } else {
            logger.warn("User not found for email: {}", email);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.debug("GET request received to fetch all users");
        List<User> users = userService.getAllUsers();
        logger.info("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        logger.debug("POST request received to create user: {}", user.getUsername());
        User createdUser = userService.saveUserWithHashedPassword(user);
        logger.info("User created successfully with id: {}", createdUser.getId());
        return ResponseEntity.ok(createdUser);
    }

    /**
     * Login endpoint for user authentication.
     * Returns a JWT token and user information upon successful authentication.
     * 
     * @param loginRequest contains username and password
     * @return LoginResponse with JWT token and user info, or 401 if invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.debug("Login attempt for username: {}", loginRequest.getUsername());
        
        try {
            User user = userService.findByUsername(loginRequest.getUsername());
            
            // Verify password using the PasswordService
            if (passwordService.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                // Generate JWT token
                String jwtToken = jwtConfig.generateToken(user.getUsername());
                
                // Create login response with token and user info
                LoginResponse loginResponse = new LoginResponse(
                    jwtToken,
                    user.getUsername(),
                    user.getEmail(),
                    user.getName(),
                    user.getLocation(),
                    user.getDateJoined() != null ? user.getDateJoined().toString() : null
                );
                
                logger.info("Successful login for user: {}", loginRequest.getUsername());
                return ResponseEntity.ok(loginResponse);
            } else {
                logger.warn("Invalid password for user: {}", loginRequest.getUsername());
                return ResponseEntity.status(401).build();
            }
        } catch (UserNotFoundException e) {
            logger.warn("Login failed - user not found: {}", loginRequest.getUsername());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            logger.error("Unexpected error during login for username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Simple login request DTO for authentication.
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
