package com.eventmaster.service;

import com.eventmaster.exception.DuplicateUserException;
import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.User;
import com.eventmaster.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
        } catch (DataIntegrityViolationException e) {
            // Parse the exception to determine which constraint was violated
            String field = determineConstraintViolation(e, user);
            logger.warn("Duplicate {} attempted: {}", field, "username".equals(field) ? user.getUsername() : user.getEmail());
            throw new DuplicateUserException(field,
                "username".equals(field) ? user.getUsername() : user.getEmail());
        } catch (Exception e) {
            logger.error("Error saving user with username: {}", user.getUsername(), e);
            throw e;
        }
    }

    /**
     * Determines which field caused the constraint violation by checking if the username or email
     * value appears in the exception message.
     *
     * @param e the DataIntegrityViolationException
     * @param user the user being saved
     * @return "username" or "email"
     */
    private String determineConstraintViolation(DataIntegrityViolationException e, User user) {
        String message = e.getMessage();
        if (message != null) {
            // Check if the email value appears in the exception message
            if (user.getEmail() != null && message.contains(user.getEmail())) {
                return "email";
            }
            // Check if the username value appears in the exception message
            if (user.getUsername() != null && message.contains(user.getUsername())) {
                return "username";
            }
        }
        // Default to username if we can't determine
        return "username";
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
