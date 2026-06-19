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

    @Query(value = "SELECT * FROM messages WHERE (sender_username = :a AND recipient_username = :b) OR (sender_username = :b AND recipient_username = :a) ORDER BY sent_at DESC LIMIT 1",
           nativeQuery = true)
    Message findLastMessageInThread(@Param("a") String a, @Param("b") String b);

    long countByRecipientUsernameAndSenderUsernameAndReadAtIsNull(String recipientUsername, String senderUsername);

    List<Message> findByRecipientUsernameAndSenderUsernameAndReadAtIsNull(String recipientUsername, String senderUsername);
}
