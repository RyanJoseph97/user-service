package com.eventmaster.repository;

import com.eventmaster.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    long countByRecipientUsernameAndSeenFalse(String recipientUsername);

    @Modifying
    @Query("UPDATE Notification n SET n.seen = true WHERE n.recipientUsername = :username AND n.seen = false")
    int markAllSeen(@Param("username") String username);
}
