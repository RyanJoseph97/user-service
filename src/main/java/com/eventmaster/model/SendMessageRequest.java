package com.eventmaster.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SendMessageRequest {

    @NotBlank
    private String recipientUsername;

    // Optional: a plain message has text; a shared event may have an empty note.
    // The service enforces that at least one of content / sharedEventId is present.
    @Size(max = 2000)
    private String content;

    // Optional id of an event being shared into the conversation.
    private String sharedEventId;

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSharedEventId() { return sharedEventId; }
    public void setSharedEventId(String sharedEventId) { this.sharedEventId = sharedEventId; }
}
