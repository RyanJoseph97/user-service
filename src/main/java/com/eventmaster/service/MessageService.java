package com.eventmaster.service;

import com.eventmaster.model.ConversationSummary;
import com.eventmaster.model.Message;
import com.eventmaster.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Message send(String senderUsername, String recipientUsername, String content) {
        // Validate recipient exists
        userService.findByUsername(recipientUsername);
        if (senderUsername.equals(recipientUsername)) {
            throw new IllegalArgumentException("Cannot message yourself");
        }
        return messageRepository.save(new Message(senderUsername, recipientUsername, content));
    }

    public List<Message> getThread(String viewerUsername, String otherUsername) {
        return messageRepository
                .findBySenderUsernameAndRecipientUsernameOrSenderUsernameAndRecipientUsernameOrderBySentAtAsc(
                        viewerUsername, otherUsername, otherUsername, viewerUsername);
    }

    @Transactional
    public void markThreadRead(String recipientUsername, String senderUsername) {
        List<Message> unread = messageRepository
                .findByRecipientUsernameAndSenderUsernameAndReadAtIsNull(recipientUsername, senderUsername);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(m -> m.setReadAt(now));
        messageRepository.saveAll(unread);
    }

    public List<ConversationSummary> getConversations(String username) {
        List<String> partners = messageRepository.findConversationPartners(username).stream()
                .distinct().collect(Collectors.toList());
        Map<String, String> profilePics = userService.findProfilePictureUrlsByUsernames(partners);
        return partners.stream()
                .map(partner -> {
                    Message last = messageRepository.findLastMessageInThread(username, partner);
                    // Defensive: a partner is only listed because a message exists, but guard
                    // against a null last message (e.g. concurrent deletion) rather than NPE.
                    if (last == null) {
                        return null;
                    }
                    long unread = messageRepository
                            .countByRecipientUsernameAndSenderUsernameAndReadAtIsNull(username, partner);
                    return new ConversationSummary(partner, profilePics.get(partner), last.getContent(), last.getSentAt(), unread);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ConversationSummary::getLastMessageAt).reversed())
                .collect(Collectors.toList());
    }
}
