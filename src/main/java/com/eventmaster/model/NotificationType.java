package com.eventmaster.model;

/**
 * Kinds of notification a user can receive. Each type belongs to a
 * {@link NotificationCategory} so the UI can group action-required items apart
 * from informational activity. New types can be added here as the app grows.
 */
public enum NotificationType {
    EVENT_INVITE(NotificationCategory.REQUEST),
    FOLLOW_REQUEST(NotificationCategory.REQUEST),
    NEW_FOLLOWER(NotificationCategory.ACTIVITY),
    EVENT_LIKE(NotificationCategory.ACTIVITY),
    EVENT_COMMENT(NotificationCategory.ACTIVITY),
    EVENT_RSVP(NotificationCategory.ACTIVITY),
    EVENT_UPDATE(NotificationCategory.ACTIVITY);

    private final NotificationCategory category;

    NotificationType(NotificationCategory category) {
        this.category = category;
    }

    public NotificationCategory getCategory() {
        return category;
    }
}
