package com.eventmaster.controller;

import com.eventmaster.model.FollowRequest;
import com.eventmaster.model.FollowRequestStatus;
import com.eventmaster.model.FollowerSummary;
import com.eventmaster.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/{username}/follow")
    public ResponseEntity<Map<String, String>> follow(@PathVariable String username, Authentication authentication) {
        boolean immediate = followService.follow(authentication.getName(), username);
        if (immediate) {
            return ResponseEntity.ok(Map.of("status", "FOLLOWING"));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", "PENDING"));
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<Void> unfollow(@PathVariable String username, Authentication authentication) {
        followService.unfollow(authentication.getName(), username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<Page<FollowerSummary>> getFollowers(@PathVariable String username,
                                                               @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowers(username, pageable));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<Page<FollowerSummary>> getFollowing(@PathVariable String username,
                                                               @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowing(username, pageable));
    }

    @GetMapping("/{username}/follow-requests")
    public ResponseEntity<List<FollowRequest>> getFollowRequests(@PathVariable String username,
                                                                  Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(followService.getPendingRequests(username));
    }

    @GetMapping("/{username}/follow-request-status")
    public ResponseEntity<Map<String, String>> getFollowRequestStatus(@PathVariable String username,
                                                                       Authentication authentication) {
        FollowRequestStatus status = followService.getRequestStatus(authentication.getName(), username);
        return ResponseEntity.ok(Map.of("status", status != null ? status.name() : "NONE"));
    }

    @PostMapping("/{username}/follow-requests/{requesterUsername}/approve")
    public ResponseEntity<Void> approveRequest(@PathVariable String username,
                                               @PathVariable String requesterUsername,
                                               Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        followService.approveRequest(username, requesterUsername);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/follow-requests/{requesterUsername}")
    public ResponseEntity<Void> rejectRequest(@PathVariable String username,
                                              @PathVariable String requesterUsername,
                                              Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        followService.rejectRequest(username, requesterUsername);
        return ResponseEntity.noContent().build();
    }
}
