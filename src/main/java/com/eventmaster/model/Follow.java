package com.eventmaster.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "followee_id"})
})
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "followee_id", nullable = false)
    private User followee;

    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt;

    public Follow() {}

    public Follow(User follower, User followee) {
        this.follower = follower;
        this.followee = followee;
        this.followedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getFollower() { return follower; }
    public User getFollowee() { return followee; }
    public LocalDateTime getFollowedAt() { return followedAt; }
}
