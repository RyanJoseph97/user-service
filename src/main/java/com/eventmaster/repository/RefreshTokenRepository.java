package com.eventmaster.repository;

import com.eventmaster.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUsernameAndRevokedFalse(String username);

    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
