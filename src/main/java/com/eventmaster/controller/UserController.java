package com.eventmaster.controller;

import com.eventmaster.model.User;
import com.eventmaster.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

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
    public User createUser(@RequestBody User user){
        logger.debug("POST request received to create user: {}", user.getUsername());
        User createdUser = userService.saveUser(user);
        logger.info("User created successfully with id: {}", createdUser.getId());
        return createdUser;
    }

}
