package com.eventmaster.repository;

import com.eventmaster.model.FollowRequest;
import com.eventmaster.model.FollowRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    Optional<FollowRequest> findByRequesterUsernameAndTargetUsername(String requesterUsername, String targetUsername);

    List<FollowRequest> findByTargetUsernameAndStatus(String targetUsername, FollowRequestStatus status);

    boolean existsByRequesterUsernameAndTargetUsernameAndStatus(
            String requesterUsername, String targetUsername, FollowRequestStatus status);

    void deleteByRequesterUsernameAndTargetUsername(String requesterUsername, String targetUsername);

    void deleteByRequesterUsername(String requesterUsername);

    void deleteByTargetUsername(String targetUsername);
}
