package com.eventmaster.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient_username", columnList = "recipient_username"),
        @Index(name = "idx_notification_recipient_seen", columnList = "recipient_username, seen")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username of the user who receives this notification. */
    @Column(name = "recipient_username", nullable = false)
    private String recipientUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /** Username of the user who triggered the notification (inviter / requester). */
    @Column(name = "actor_username")
    private String actorUsername;

    /** Optional reference to the related entity (e.g. event id for an invite). */
    @Column(name = "entity_id")
    private String entityId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private boolean seen = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(String recipientUsername, NotificationType type, String actorUsername,
                        String entityId, String message) {
        this.recipientUsername = recipientUsername;
        this.type = type;
        this.actorUsername = actorUsername;
        this.entityId = entityId;
        this.message = message;
        this.seen = false;
        this.createdAt = LocalDateTime.now();
    }

    /** Derived from {@link #type}; lets the UI group notifications without a stored column. */
    @Transient
    public NotificationCategory getCategory() {
        return type != null ? type.getCategory() : null;
    }

    public Long getId() { return id; }
    public String getRecipientUsername() { return recipientUsername; }
    public NotificationType getType() { return type; }
    public String getActorUsername() { return actorUsername; }
    public String getEntityId() { return entityId; }
    public String getMessage() { return message; }
    public boolean isSeen() { return seen; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setSeen(boolean seen) { this.seen = seen; }
}
