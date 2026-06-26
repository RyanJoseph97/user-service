package com.eventmaster.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventmaster.model.Follow;
import com.eventmaster.model.FollowRequest;
import com.eventmaster.model.FollowRequestStatus;
import com.eventmaster.model.FollowerSummary;
import com.eventmaster.model.NotificationType;
import com.eventmaster.model.User;
import com.eventmaster.repository.FollowRepository;
import com.eventmaster.repository.FollowRequestRepository;

@Service
public class FollowService {
    private static final Logger logger = LoggerFactory.getLogger(FollowService.class);

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FollowRequestRepository followRequestRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Follow or request-to-follow based on the target's privacy setting.
     * Returns true if the follow was immediate, false if a request was created.
     */
    @Transactional
    public boolean follow(String followerUsername, String followeeUsername) {
        if (followerUsername.equals(followeeUsername)) {
            throw new IllegalArgumentException("Users cannot follow themselves");
        }

        User follower = userService.findByUsername(followerUsername);
        User followee = userService.findByUsername(followeeUsername);

        if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new IllegalStateException("Already following " + followeeUsername);
        }

        if (followee.isPrivateProfile()) {
            // Reuse any existing request row rather than delete + re-insert: the unique
            // constraint on (requester_username, target_username) plus IDENTITY insert timing
            // would otherwise risk a constraint violation on save.
            FollowRequest request = followRequestRepository
                    .findByRequesterUsernameAndTargetUsername(followerUsername, followeeUsername)
                    .map(existing -> {
                        if (existing.getStatus() == FollowRequestStatus.PENDING) {
                            throw new IllegalStateException("Follow request already pending");
                        }
                        // Resurrect a previously resolved request as a fresh pending one
                        existing.setStatus(FollowRequestStatus.PENDING);
                        existing.setCreatedAt(LocalDateTime.now());
                        return existing;
                    })
                    .orElseGet(() -> new FollowRequest(followerUsername, followeeUsername));
            followRequestRepository.save(request);
            notificationService.create(
                    followeeUsername,
                    NotificationType.FOLLOW_REQUEST,
                    followerUsername,
                    followerUsername,
                    "@" + followerUsername + " requested to follow you");
            logger.info("{} requested to follow {}", followerUsername, followeeUsername);
            return false;
        }

        followRepository.save(new Follow(follower, followee));
        notificationService.create(
                followeeUsername,
                NotificationType.NEW_FOLLOWER,
                followerUsername,
                followerUsername,
                "@" + followerUsername + " started following you");
        logger.info("{} followed {}", followerUsername, followeeUsername);
        return true;
    }

    @Transactional
    public void unfollow(String followerUsername, String followeeUsername) {
        User follower = userService.findByUsername(followerUsername);
        User followee = userService.findByUsername(followeeUsername);

        boolean wasFollowing = followRepository.existsByFollowerAndFollowee(follower, followee);
        if (wasFollowing) {
            followRepository.deleteByFollowerAndFollowee(follower, followee);
            logger.info("{} unfollowed {}", followerUsername, followeeUsername);
        }
        // Also cancel any pending request
        followRequestRepository.deleteByRequesterUsernameAndTargetUsername(followerUsername, followeeUsername);
    }

    @Transactional
    public void approveRequest(String targetUsername, String requesterUsername) {
        FollowRequest req = followRequestRepository
                .findByRequesterUsernameAndTargetUsername(requesterUsername, targetUsername)
                .orElseThrow(() -> new IllegalStateException("No pending follow request found"));
        if (req.getStatus() != FollowRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        User follower = userService.findByUsername(requesterUsername);
        User followee = userService.findByUsername(targetUsername);
        followRepository.save(new Follow(follower, followee));
        req.setStatus(FollowRequestStatus.APPROVED);
        followRequestRepository.save(req);
        logger.info("{} approved follow request from {}", targetUsername, requesterUsername);
    }

    @Transactional
    public void rejectRequest(String targetUsername, String requesterUsername) {
        followRequestRepository.deleteByRequesterUsernameAndTargetUsername(requesterUsername, targetUsername);
        logger.info("{} rejected/cancelled follow request from {}", targetUsername, requesterUsername);
    }

    public List<FollowRequest> getPendingRequests(String targetUsername) {
        return followRequestRepository.findByTargetUsernameAndStatus(targetUsername, FollowRequestStatus.PENDING);
    }

    public FollowRequestStatus getRequestStatus(String requesterUsername, String targetUsername) {
        return followRequestRepository
                .findByRequesterUsernameAndTargetUsername(requesterUsername, targetUsername)
                .map(FollowRequest::getStatus)
                .orElse(null);
    }

    public Page<FollowerSummary> getFollowers(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        return followRepository.findByFollowee(user, pageable)
                .map(f -> new FollowerSummary(
                        f.getFollower().getUsername(),
                        f.getFollower().getName(),
                        f.getFollower().getDateJoined(),
                        f.getFollower().getProfilePictureUrl()));
    }

    public Page<FollowerSummary> getFollowing(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        return followRepository.findByFollower(user, pageable)
                .map(f -> new FollowerSummary(
                        f.getFollowee().getUsername(),
                        f.getFollowee().getName(),
                        f.getFollowee().getDateJoined(),
                        f.getFollowee().getProfilePictureUrl()));
    }
}
