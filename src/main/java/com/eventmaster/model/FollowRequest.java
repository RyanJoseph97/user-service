package com.eventmaster.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follow_requests", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"requester_username", "target_username"})
})
public class FollowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_username", nullable = false)
    private String requesterUsername;

    @Column(name = "target_username", nullable = false)
    private String targetUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowRequestStatus status = FollowRequestStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FollowRequest() {}

    public FollowRequest(String requesterUsername, String targetUsername) {
        this.requesterUsername = requesterUsername;
        this.targetUsername = targetUsername;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getRequesterUsername() { return requesterUsername; }
    public String getTargetUsername() { return targetUsername; }
    public FollowRequestStatus getStatus() { return status; }
    public void setStatus(FollowRequestStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
