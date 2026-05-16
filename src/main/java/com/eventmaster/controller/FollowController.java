package com.eventmaster.controller;

import com.eventmaster.model.FollowerSummary;
import com.eventmaster.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class FollowController {

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
    public ResponseEntity<Page<FollowerSummary>> getFollowers(@PathVariable String username,
                                                               @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowers(username, pageable));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<Page<FollowerSummary>> getFollowing(@PathVariable String username,
                                                               @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowing(username, pageable));
    }
}
