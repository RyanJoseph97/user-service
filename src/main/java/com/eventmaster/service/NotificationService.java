package com.eventmaster.service;

import com.eventmaster.model.Notification;
import com.eventmaster.model.NotificationType;
import com.eventmaster.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Notification create(String recipientUsername, NotificationType type, String actorUsername,
                               String entityId, String message) {
        return notificationRepository.save(
                new Notification(recipientUsername, type, actorUsername, entityId, message));
    }

    public List<Notification> list(String recipientUsername) {
        return notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(recipientUsername);
    }

    public long unseenCount(String recipientUsername) {
        return notificationRepository.countByRecipientUsernameAndSeenFalse(recipientUsername);
    }

    @Transactional
    public void markAllSeen(String recipientUsername) {
        notificationRepository.markAllSeen(recipientUsername);
    }
}
