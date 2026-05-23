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

import com.eventmaster.model.AccountStatus;
import com.eventmaster.model.ChangePasswordRequest;
import com.eventmaster.model.UpdateUserRequest;
import com.eventmaster.repository.FollowRepository;
import com.eventmaster.repository.FollowRequestRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FollowRequestRepository followRequestRepository;

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
     * Save a user with automatic password hashing.
     * This method should be used when creating users from user input to ensure
     * passwords are properly hashed before being stored in the database.
     * 
     * @param user the user to save (password will be hashed automatically)
     * @return the saved user with hashed password
     */
    public User saveUserWithHashedPassword(User user) {
        logger.info("Attempting to save user with username: {} (password will be hashed)", user.getUsername());
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or blank");
        }

        user.setDateJoined(LocalDate.now());
        user.setPassword(passwordService.hashPassword(user.getPassword()));
        logger.debug("Password hashed for user: {}", user.getUsername());

        return saveUser(user);
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

    public User verifyUser(String username) {
        User user = findByUsername(username);
        user.setAccountStatus(AccountStatus.VERIFIED);
        return userRepository.save(user);
    }

    public User updateUser(String username, UpdateUserRequest request) {
        User user = findByUsername(username);
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getName() != null) user.setName(request.getName());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getPrivateProfile() != null) user.setPrivateProfile(request.getPrivateProfile());
        if (request.getProfilePictureUrl() != null) user.setProfilePictureUrl(request.getProfilePictureUrl());
        return saveUser(user);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findByUsername(username);
        if (!passwordService.verifyPassword(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordService.hashPassword(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed for user: {}", username);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteUser(String username) {
        User user = findByUsername(username);
        followRepository.deleteByFollower(user);
        followRepository.deleteByFollowee(user);
        followRequestRepository.deleteByRequesterUsername(username);
        followRequestRepository.deleteByTargetUsername(username);
        userRepository.delete(user);
        logger.info("Deleted user: {}", username);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        logger.debug("Fetching all users");
        Page<User> users = userRepository.findAll(pageable);
        logger.info("Retrieved {} users from database", users.getTotalElements());
        return users;
    }

    public List<User> searchUsers(String q, Pageable pageable) {
        logger.debug("Searching users with query: {}", q);
        return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(q, q, pageable).getContent();
    }
}
