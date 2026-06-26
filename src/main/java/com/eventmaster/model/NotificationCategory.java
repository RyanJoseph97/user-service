package com.eventmaster.model;

/**
 * Grouping used to keep high-signal, action-required notifications (invites,
 * follow requests) from being buried under high-volume activity notifications
 * (likes, comments, RSVPs, new followers).
 */
public enum NotificationCategory {
    /** Needs the user to do something — surfaced prominently. */
    REQUEST,
    /** Informational activity on the user's content or profile. */
    ACTIVITY
}
