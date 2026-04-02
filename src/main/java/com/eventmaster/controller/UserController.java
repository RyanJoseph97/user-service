package com.eventmaster.controller;

import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.ChangePasswordRequest;
import com.eventmaster.model.CreateUserRequest;
import com.eventmaster.model.UpdateUserRequest;
import com.eventmaster.model.User;
import com.eventmaster.model.LoginResponse;
import com.eventmaster.service.UserService;
import com.eventmaster.service.PasswordService;
import com.eventmaster.jwt.JwtConfig;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${admin.username}")
    private String adminUsername;

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
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.debug("POST request received to create user: {}", request.getUsername());
        if (request.getUsername().equals(adminUsername)) {
            logger.warn("Attempt to register reserved admin username: {}", adminUsername);
            return ResponseEntity.status(403).build();
        }
        User user = new User(request.getUsername(), request.getPassword(),
                request.getEmail(), request.getName(), request.getLocation());
        User createdUser = userService.saveUserWithHashedPassword(user);
        logger.info("User created successfully with id: {}", createdUser.getId());
        return ResponseEntity.ok(createdUser);
    }

    @PatchMapping("/{username}/verify")
    public ResponseEntity<User> verifyUser(@PathVariable String username, Authentication authentication) {
        if (!authentication.getName().equals(adminUsername)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.verifyUser(username));
    }

    @PatchMapping("/{username}")
    public ResponseEntity<User> updateUser(@PathVariable String username,
                                           @Valid @RequestBody UpdateUserRequest request,
                                           Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.updateUser(username, request));
    }

    @PatchMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(@PathVariable String username,
                                               @Valid @RequestBody ChangePasswordRequest request,
                                               Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        userService.changePassword(username, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username,
                                           Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
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
                    user.getDateJoined().toString()
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
