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
    public void getConversations_nullLastMessage_skipsPartnerWithoutNpe() {
        when(messageRepository.findConversationPartners("alice")).thenReturn(List.of("bob"));
        when(messageRepository.findLastMessageInThread("alice", "bob")).thenReturn(null);
        when(userService.findProfilePictureUrlsByUsernames(List.of("bob")))
                .thenReturn(java.util.Collections.singletonMap("bob", null));

        var convs = messageService.getConversations("alice");

        // The partner with no resolvable last message is skipped rather than causing an NPE
        assertTrue(convs.isEmpty());
    }

    @Test
    public void getConversations_ordersByMostRecentLastMessageFirst() {
        // Partners come back in [bob, carol] order; bob's last message is older, so the
        // result must be re-sorted to put carol (more recent) first.
        when(messageRepository.findConversationPartners("alice")).thenReturn(List.of("bob", "carol"));

        Message lastFromBob = mock(Message.class);
        when(lastFromBob.getContent()).thenReturn("older");
        when(lastFromBob.getSentAt()).thenReturn(LocalDateTime.of(2024, 1, 1, 10, 0));
        Message lastFromCarol = mock(Message.class);
        when(lastFromCarol.getContent()).thenReturn("newer");
        when(lastFromCarol.getSentAt()).thenReturn(LocalDateTime.of(2024, 1, 2, 10, 0));

        when(messageRepository.findLastMessageInThread("alice", "bob")).thenReturn(lastFromBob);
        when(messageRepository.findLastMessageInThread("alice", "carol")).thenReturn(lastFromCarol);
        when(messageRepository.countByRecipientUsernameAndSenderUsernameAndReadAtIsNull(eq("alice"), anyString()))
                .thenReturn(0L);
        when(userService.findProfilePictureUrlsByUsernames(List.of("bob", "carol")))
                .thenReturn(java.util.Collections.emptyMap());

        var convs = messageService.getConversations("alice");

        assertEquals(2, convs.size());
        assertEquals("carol", convs.get(0).getOtherUsername());
        assertEquals("bob", convs.get(1).getOtherUsername());
    }

    @Test
    public void getConversations_reportsUnreadCountPerPartner() {
        when(messageRepository.findConversationPartners("alice")).thenReturn(List.of("bob"));
        Message last = mock(Message.class);
        when(last.getContent()).thenReturn("hey");
        when(last.getSentAt()).thenReturn(LocalDateTime.of(2024, 1, 1, 10, 0));
        when(messageRepository.findLastMessageInThread("alice", "bob")).thenReturn(last);
        when(messageRepository.countByRecipientUsernameAndSenderUsernameAndReadAtIsNull("alice", "bob"))
                .thenReturn(3L);
        when(userService.findProfilePictureUrlsByUsernames(List.of("bob")))
                .thenReturn(java.util.Collections.emptyMap());

        var convs = messageService.getConversations("alice");

        assertEquals(1, convs.size());
        assertEquals(3L, convs.get(0).getUnreadCount());
    }
}
