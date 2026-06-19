package com.eventmaster.model;

import java.time.LocalDateTime;

public class ConversationSummary {

    private String otherUsername;
    private String otherProfilePictureUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;

    public ConversationSummary(String otherUsername, String otherProfilePictureUrl, String lastMessage, LocalDateTime lastMessageAt, long unreadCount) {
        this.otherUsername = otherUsername;
        this.otherProfilePictureUrl = otherProfilePictureUrl;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }

    public String getOtherUsername() { return otherUsername; }
    public String getOtherProfilePictureUrl() { return otherProfilePictureUrl; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public long getUnreadCount() { return unreadCount; }
}
