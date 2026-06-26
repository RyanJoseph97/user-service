package com.eventmaster.controller;

import com.eventmaster.exception.InvalidRefreshTokenException;
import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.ChangePasswordRequest;
import com.eventmaster.model.CreateUserRequest;
import com.eventmaster.model.LoginResponse;
import com.eventmaster.model.RefreshToken;
import com.eventmaster.model.UpdateUserRequest;
import com.eventmaster.model.User;
import com.eventmaster.model.UserSearchResult;
import com.eventmaster.service.PasswordService;
import com.eventmaster.service.RefreshTokenService;
import com.eventmaster.service.UserService;
import com.eventmaster.jwt.JwtConfig;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${admin.username}")
    private String adminUsername;

    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtConfig jwtConfig;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public UserController(UserService userService, PasswordService passwordService,
                          JwtConfig jwtConfig, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtConfig = jwtConfig;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
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
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        logger.debug("GET request received for username: {}", username);
        User user = userService.findByUsername(username);
        if (user != null) {
            logger.info("Returning user for username: {}", username);
            return ResponseEntity.ok(user);
        } else {
            logger.warn("User not found for username: {}", username);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email, Authentication authentication) {
        if (authentication == null || !adminUsername.equals(authentication.getName())) {
            return ResponseEntity.status(403).build();
        }
        logger.debug("GET request received for email: {}", email);
        User user = userService.findByEmail(email);
        if (user != null) {
            logger.info("Returning user for email: {}", email);
            return ResponseEntity.ok(user);
        } else {
            logger.warn("User not found for email: {}", email);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        logger.debug("GET request received to fetch all users");
        Page<User> users = userService.getAllUsers(pageable);
        logger.info("Returning {} users", users.getTotalElements());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResult>> searchUsers(@RequestParam String q) {
        logger.debug("GET /users/search q={}", q);
        List<UserSearchResult> results = userService.searchUsers(q).stream()
                .map(UserSearchResult::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.debug("Login attempt for username: {}", loginRequest.getUsername());
        try {
            User user = userService.findByUsername(loginRequest.getUsername());
            if (passwordService.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("accountStatus", user.getAccountStatus().name());
                String accessToken = jwtConfig.generateToken(user.getUsername(), claims);
                RefreshToken rt = refreshTokenService.createFor(user.getUsername());

                LoginResponse response = new LoginResponse(
                        accessToken,
                        rt.getToken(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getName(),
                        user.getLocation(),
                        user.getDateJoined().toString(),
                        user.getAccountStatus().name()
                );
                logger.info("Successful login for user: {}", loginRequest.getUsername());
                return ResponseEntity.ok(response);
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

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        try {
            RefreshToken newRt = refreshTokenService.rotate(request.getRefreshToken());
            User user = userService.findByUsername(newRt.getUsername());
            Map<String, Object> claims = new HashMap<>();
            claims.put("accountStatus", user.getAccountStatus().name());
            String newAccessToken = jwtConfig.generateToken(newRt.getUsername(), claims);
            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRt.getToken()));
        } catch (InvalidRefreshTokenException e) {
            logger.warn("Token refresh rejected: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class TokenRefreshRequest {
        private String refreshToken;

        public TokenRefreshRequest() {}

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class TokenRefreshResponse {
        private String accessToken;
        private String refreshToken;

        public TokenRefreshResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }

    public static class LogoutRequest {
        private String refreshToken;

        public LogoutRequest() {}

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}
