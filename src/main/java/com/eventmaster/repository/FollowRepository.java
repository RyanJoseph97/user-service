package com.eventmaster.repository;

import com.eventmaster.model.Follow;
import com.eventmaster.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Page<Follow> findByFollowee(User followee, Pageable pageable);
    Page<Follow> findByFollower(User follower, Pageable pageable);
    boolean existsByFollowerAndFollowee(User follower, User followee);
    void deleteByFollowerAndFollowee(User follower, User followee);
    void deleteByFollower(User follower);
    void deleteByFollowee(User followee);
}
