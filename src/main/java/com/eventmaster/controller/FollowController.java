package com.eventmaster.controller;

import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.FollowerSummary;
import com.eventmaster.service.FollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class FollowController {
    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private FollowService followService;

    @PostMapping("/{username}/follow")
    public ResponseEntity<Void> follow(@PathVariable String username, Authentication authentication) {
        String currentUser = authentication.getName();
        try {
            followService.follow(currentUser, username);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Bad follow request by {}: {}", currentUser, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.warn("Conflict on follow by {}: {}", currentUser, e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found during follow: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<Void> unfollow(@PathVariable String username, Authentication authentication) {
        String currentUser = authentication.getName();
        try {
            followService.unfollow(currentUser, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            logger.warn("Conflict on unfollow by {}: {}", currentUser, e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found during unfollow: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<List<FollowerSummary>> getFollowers(@PathVariable String username) {
        try {
            return ResponseEntity.ok(followService.getFollowers(username));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<List<FollowerSummary>> getFollowing(@PathVariable String username) {
        try {
            return ResponseEntity.ok(followService.getFollowing(username));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
