package com.eventmaster.repository;
import com.eventmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    // Make sure there is no custom query defined for findAll()
    List<User> findAll();
}