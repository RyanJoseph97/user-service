package com.eventmaster.controller;

import com.eventmaster.model.Notification;
import com.eventmaster.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> list(Authentication authentication) {
        return ResponseEntity.ok(notificationService.list(authentication.getName()));
    }

    @GetMapping("/unseen-count")
    public ResponseEntity<Map<String, Long>> unseenCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", notificationService.unseenCount(authentication.getName())));
    }

    @PostMapping("/mark-seen")
    public ResponseEntity<Void> markSeen(Authentication authentication) {
        notificationService.markAllSeen(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
