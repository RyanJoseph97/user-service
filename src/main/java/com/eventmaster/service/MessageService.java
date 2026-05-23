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
        List<String> partners = messageRepository.findConversationPartners(username);
        return partners.stream()
                .distinct()
                .map(partner -> {
                    Message last = messageRepository.findLastMessageInThread(username, partner);
                    long unread = messageRepository
                            .countByRecipientUsernameAndSenderUsernameAndReadAtIsNull(username, partner);
                    return new ConversationSummary(partner, last.getContent(), last.getSentAt(), unread);
                })
                .sorted(Comparator.comparing(ConversationSummary::getLastMessageAt).reversed())
                .collect(Collectors.toList());
    }
}
