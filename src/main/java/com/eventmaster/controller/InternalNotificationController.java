package com.eventmaster.controller;

import com.eventmaster.model.CreateNotificationRequest;
import com.eventmaster.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Network-internal endpoint for other services to create notifications.
 *
 * This path is NOT routed by the api-gateway, so it is unreachable from the public
 * internet — only sibling services calling user-service directly (within the Docker
 * network) can post here. It is permitted without JWT in SecurityConfig to keep
 * cross-service calls simple, mirroring the existing direct service-to-service calls.
 */
@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateNotificationRequest request) {
        notificationService.create(
                request.getRecipientUsername(),
                request.getType(),
                request.getActorUsername(),
                request.getEntityId(),
                request.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
