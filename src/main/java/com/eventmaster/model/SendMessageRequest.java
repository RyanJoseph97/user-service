package com.eventmaster.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SendMessageRequest {

    @NotBlank
    private String recipientUsername;

    @NotBlank
    @Size(max = 2000)
    private String content;

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
