package com.eventmaster.controller;

import com.eventmaster.model.ConversationSummary;
import com.eventmaster.model.Message;
import com.eventmaster.model.SendMessageRequest;
import com.eventmaster.service.MessageService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<Message> send(@Valid @RequestBody SendMessageRequest request,
                                        Authentication authentication) {
        Message msg = messageService.send(authentication.getName(), request.getRecipientUsername(),
                request.getContent(), request.getSharedEventId());
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummary>> conversations(Authentication authentication) {
        return ResponseEntity.ok(messageService.getConversations(authentication.getName()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<Message>> thread(@PathVariable String username,
                                                Authentication authentication) {
        String me = authentication.getName();
        List<Message> messages = messageService.getThread(me, username);
        messageService.markThreadRead(me, username);
        return ResponseEntity.ok(messages);
    }
}
