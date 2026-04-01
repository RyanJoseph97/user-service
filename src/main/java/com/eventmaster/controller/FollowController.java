package com.eventmaster.controller;

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
        followService.follow(authentication.getName(), username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<Void> unfollow(@PathVariable String username, Authentication authentication) {
        followService.unfollow(authentication.getName(), username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<List<FollowerSummary>> getFollowers(@PathVariable String username) {
        return ResponseEntity.ok(followService.getFollowers(username));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<List<FollowerSummary>> getFollowing(@PathVariable String username) {
        return ResponseEntity.ok(followService.getFollowing(username));
    }
}
