package com.eventmaster.service;

import com.eventmaster.model.Follow;
import com.eventmaster.model.FollowerSummary;
import com.eventmaster.model.User;
import com.eventmaster.repository.FollowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {
    private static final Logger logger = LoggerFactory.getLogger(FollowService.class);

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void follow(String followerUsername, String followeeUsername) {
        if (followerUsername.equals(followeeUsername)) {
            throw new IllegalArgumentException("Users cannot follow themselves");
        }

        User follower = userService.findByUsername(followerUsername);
        User followee = userService.findByUsername(followeeUsername);

        if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new IllegalStateException("Already following " + followeeUsername);
        }

        followRepository.save(new Follow(follower, followee));
        logger.info("{} followed {}", followerUsername, followeeUsername);
    }

    @Transactional
    public void unfollow(String followerUsername, String followeeUsername) {
        User follower = userService.findByUsername(followerUsername);
        User followee = userService.findByUsername(followeeUsername);

        if (!followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new IllegalStateException("Not following " + followeeUsername);
        }

        followRepository.deleteByFollowerAndFollowee(follower, followee);
        logger.info("{} unfollowed {}", followerUsername, followeeUsername);
    }

    public List<FollowerSummary> getFollowers(String username) {
        User user = userService.findByUsername(username);
        return followRepository.findByFollowee(user).stream()
                .map(f -> new FollowerSummary(
                        f.getFollower().getUsername(),
                        f.getFollower().getName(),
                        f.getFollower().getDateJoined()))
                .collect(Collectors.toList());
    }

    public List<FollowerSummary> getFollowing(String username) {
        User user = userService.findByUsername(username);
        return followRepository.findByFollower(user).stream()
                .map(f -> new FollowerSummary(
                        f.getFollowee().getUsername(),
                        f.getFollowee().getName(),
                        f.getFollowee().getDateJoined()))
                .collect(Collectors.toList());
    }
}
