package com.eventmaster.service;

import com.eventmaster.model.Notification;
import com.eventmaster.model.NotificationType;
import com.eventmaster.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void create_savesNotificationAndReturnsIt() {
        Notification saved = new Notification("alice", NotificationType.EVENT_COMMENT, "bob", "1",
                "@bob commented on your event");
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        Notification result = notificationService.create("alice", NotificationType.EVENT_COMMENT,
                "bob", "1", "@bob commented on your event");

        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void list_returnsNotificationsInDescOrder() {
        Notification n1 = new Notification("alice", NotificationType.EVENT_COMMENT, "bob", "1", "msg1");
        Notification n2 = new Notification("alice", NotificationType.NEW_FOLLOWER, "carol", null, "msg2");
        when(notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc("alice"))
                .thenReturn(List.of(n1, n2));

        List<Notification> result = notificationService.list("alice");

        assertEquals(2, result.size());
        assertEquals(n1, result.get(0));
        verify(notificationRepository).findByRecipientUsernameOrderByCreatedAtDesc("alice");
    }

    @Test
    public void list_noNotifications_returnsEmptyList() {
        when(notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc("bob"))
                .thenReturn(List.of());

        List<Notification> result = notificationService.list("bob");

        assertTrue(result.isEmpty());
    }

    @Test
    public void unseenCount_returnsCorrectCount() {
        when(notificationRepository.countByRecipientUsernameAndSeenFalse("alice")).thenReturn(3L);

        long count = notificationService.unseenCount("alice");

        assertEquals(3L, count);
        verify(notificationRepository).countByRecipientUsernameAndSeenFalse("alice");
    }

    @Test
    public void unseenCount_noneUnseen_returnsZero() {
        when(notificationRepository.countByRecipientUsernameAndSeenFalse("alice")).thenReturn(0L);

        assertEquals(0L, notificationService.unseenCount("alice"));
    }

    @Test
    public void markAllSeen_callsRepository() {
        notificationService.markAllSeen("alice");

        verify(notificationRepository).markAllSeen("alice");
    }
}
