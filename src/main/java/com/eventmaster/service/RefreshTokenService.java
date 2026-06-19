package com.eventmaster.service;

import com.eventmaster.exception.InvalidRefreshTokenException;
import com.eventmaster.model.RefreshToken;
import com.eventmaster.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${refresh.token.expiration.days:30}")
    private int expirationDays;

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public RefreshToken createFor(String username) {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime now = LocalDateTime.now();
        RefreshToken rt = new RefreshToken(tokenValue, username, now, now.plusDays(expirationDays));
        return repo.save(rt);
    }

    @Transactional
    public RefreshToken rotate(String oldTokenValue) {
        RefreshToken old = repo.findByToken(oldTokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (old.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        if (old.isRevoked()) {
            // Replay attack: revoke all active tokens for this user
            logger.warn("Replay attack detected for user '{}' — revoking all sessions", old.getUsername());
            revokeAll(old.getUsername());
            throw new InvalidRefreshTokenException("Refresh token already used");
        }

        old.setRevoked(true);
        repo.save(old);
        return createFor(old.getUsername());
    }

    @Transactional
    public void revoke(String tokenValue) {
        repo.findByToken(tokenValue).ifPresent(rt -> {
            rt.setRevoked(true);
            repo.save(rt);
        });
    }

    @Transactional
    public void revokeAll(String username) {
        List<RefreshToken> active = repo.findByUsernameAndRevokedFalse(username);
        active.forEach(rt -> rt.setRevoked(true));
        repo.saveAll(active);
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void purgeExpired() {
        repo.deleteByExpiresAtBefore(LocalDateTime.now().minusDays(1));
        logger.info("Purged expired refresh tokens");
    }
}
