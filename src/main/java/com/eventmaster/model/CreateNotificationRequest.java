package com.eventmaster.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Payload for the internal notification-creation endpoint used by other services
 * (e.g. event-service when an invite is sent).
 */
public class CreateNotificationRequest {

    @NotBlank
    private String recipientUsername;

    @NotNull
    private NotificationType type;

    private String actorUsername;
    private String entityId;

    @NotBlank
    private String message;

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
