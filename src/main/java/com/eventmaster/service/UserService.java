package com.eventmaster.service;

import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.User;
import com.eventmaster.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        logger.info("Attempting to save user with username: {}", user.getUsername());
        try {
            User savedUser = userRepository.save(user);
            logger.info("Successfully saved user with id: {} and username: {}", savedUser.getId(), savedUser.getUsername());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error saving user with username: {}", user.getUsername(), e);
            throw e;
        }
    }

    public User findByUsername(String username){
        logger.debug("Searching for user by username: {}", username);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> UserNotFoundException.byUsername(username));
            logger.info("Found user with username: {}", username);
            return user;
        } catch (UserNotFoundException e) {
            logger.warn("User not found with username: {}", username);
            throw e;
        }
    }

    public User findByEmail(String email){
        logger.debug("Searching for user by email: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> UserNotFoundException.byEmail(email));
            logger.info("Found user with email: {}", email);
            return user;
        } catch (UserNotFoundException e) {
            logger.warn("User not found with email: {}", email);
            throw e;
        }
    }

    public Optional<User> findById(Long id){
        logger.debug("Searching for user by id: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.info("Found user with id: {}", id);
        } else {
            logger.debug("User not found with id: {}", id);
        }
        return user;
    }

    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.info("Retrieved {} users from database", users.size());
        return users;
    }
}
