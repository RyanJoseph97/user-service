package com.eventmaster.service;

import com.eventmaster.exception.UserNotFoundException;
import com.eventmaster.model.Message;
import com.eventmaster.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --- send ---

    @Test
    public void send_validMessage_savesAndReturns() {
        Message saved = new Message("alice", "bob", "Hello!");
        when(messageRepository.save(any(Message.class))).thenReturn(saved);

        Message result = messageService.send("alice", "bob", "Hello!");

        assertEquals("alice", result.getSenderUsername());
        assertEquals("bob", result.getRecipientUsername());
        verify(userService).findByUsername("bob");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    public void send_selfMessage_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> messageService.send("alice", "alice", "Hi me"));

        verify(messageRepository, never()).save(any());
    }

    @Test
    public void send_recipientNotFound_throwsUserNotFoundException() {
        doThrow(UserNotFoundException.byUsername("ghost"))
                .when(userService).findByUsername("ghost");

        assertThrows(UserNotFoundException.class,
                () -> messageService.send("alice", "ghost", "Hello?"));

        verify(messageRepository, never()).save(any());
    }

    // --- getThread ---

    @Test
    public void getThread_returnsOrderedMessages() {
        Message m1 = new Message("alice", "bob", "Hi");
        Message m2 = new Message("bob", "alice", "Hey");
        when(messageRepository
                .findBySenderUsernameAndRecipientUsernameOrSenderUsernameAndRecipientUsernameOrderBySentAtAsc(
                        "alice", "bob", "bob", "alice"))
                .thenReturn(List.of(m1, m2));

        List<Message> thread = messageService.getThread("alice", "bob");

        assertEquals(2, thread.size());
        verify(messageRepository)
                .findBySenderUsernameAndRecipientUsernameOrSenderUsernameAndRecipientUsernameOrderBySentAtAsc(
                        "alice", "bob", "bob", "alice");
    }

    // --- markThreadRead ---

    @Test
    public void markThreadRead_setsReadAt() {
        Message unread = new Message("alice", "bob", "Hi");
        when(messageRepository.findByRecipientUsernameAndSenderUsernameAndReadAtIsNull("bob", "alice"))
                .thenReturn(List.of(unread));

        messageService.markThreadRead("bob", "alice");

        assertNotNull(unread.getReadAt());
        verify(messageRepository).saveAll(List.of(unread));
    }

    @Test
    public void markThreadRead_noUnread_doesNothing() {
        when(messageRepository.findByRecipientUsernameAndSenderUsernameAndReadAtIsNull("bob", "alice"))
                .thenReturn(List.of());

        messageService.markThreadRead("bob", "alice");

        verify(messageRepository).saveAll(List.of());
    }

    // --- getConversations ---

    @Test
    public void getConversations_nullLastMessage_doesNotNpe() {
        when(messageRepository.findConversationPartners("alice")).thenReturn(List.of("bob"));
        Message last = new Message("alice", "bob", "Yo");
        when(messageRepository.findLastMessageInThread("alice", "bob")).thenReturn(last);
        when(messageRepository.countByRecipientUsernameAndSenderUsernameAndReadAtIsNull("alice", "bob"))
                .thenReturn(0L);
        when(userService.findProfilePictureUrlsByUsernames(List.of("bob")))
                .thenReturn(java.util.Collections.singletonMap("bob", null));

        var convs = messageService.getConversations("alice");

        assertEquals(1, convs.size());
        assertEquals("bob", convs.get(0).getOtherUsername());
    }
}
