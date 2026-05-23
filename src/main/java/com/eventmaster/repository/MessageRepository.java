package com.eventmaster.repository;

import com.eventmaster.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderUsernameAndRecipientUsernameOrSenderUsernameAndRecipientUsernameOrderBySentAtAsc(
            String sender1, String recipient1, String sender2, String recipient2);

    @Query("SELECT DISTINCT CASE WHEN m.senderUsername = :username THEN m.recipientUsername ELSE m.senderUsername END " +
           "FROM Message m WHERE m.senderUsername = :username OR m.recipientUsername = :username")
    List<String> findConversationPartners(@Param("username") String username);

    @Query("SELECT m FROM Message m WHERE " +
           "((m.senderUsername = :a AND m.recipientUsername = :b) OR (m.senderUsername = :b AND m.recipientUsername = :a)) " +
           "ORDER BY m.sentAt DESC LIMIT 1")
    Message findLastMessageInThread(@Param("a") String a, @Param("b") String b);

    long countByRecipientUsernameAndSenderUsernameAndReadAtIsNull(String recipientUsername, String senderUsername);

    List<Message> findByRecipientUsernameAndSenderUsernameAndReadAtIsNull(String recipientUsername, String senderUsername);
}
