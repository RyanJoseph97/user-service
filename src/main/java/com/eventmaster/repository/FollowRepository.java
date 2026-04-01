package com.eventmaster.repository;

import com.eventmaster.model.Follow;
import com.eventmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByFollowee(User followee);
    List<Follow> findByFollower(User follower);
    boolean existsByFollowerAndFollowee(User follower, User followee);
    void deleteByFollowerAndFollowee(User follower, User followee);
}
