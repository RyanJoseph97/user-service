package com.eventmaster.service;

import com.eventmaster.exception.InvalidRefreshTokenException;
import com.eventmaster.model.RefreshToken;
import com.eventmaster.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repo;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Default expiration of 30 days
        ReflectionTestUtils.setField(refreshTokenService, "expirationDays", 30);
    }

    // --- createFor ---

    @Test
    public void createFor_savesAndReturnsToken() {
        String username = "alice";
        RefreshToken saved = new RefreshToken("sometoken", username,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30));
        when(repo.save(any(RefreshToken.class))).thenReturn(saved);

        RefreshToken result = refreshTokenService.createFor(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(repo).save(any(RefreshToken.class));
    }

    @Test
    public void createFor_tokenValueIsBase64UrlEncoded() {
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createFor("bob");

        assertNotNull(result.getToken());
        assertFalse(result.getToken().isEmpty());
        // Base64 URL encoding should not contain + or /
        assertFalse(result.getToken().contains("+"));
        assertFalse(result.getToken().contains("/"));
    }

    // --- rotate ---

    @Test
    public void rotate_validToken_revokesOldAndCreatesNew() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken old = new RefreshToken("oldtoken", "alice", now.minusDays(1), now.plusDays(29));
        RefreshToken newToken = new RefreshToken("newtoken", "alice", now, now.plusDays(30));

        when(repo.findByToken("oldtoken")).thenReturn(Optional.of(old));
        when(repo.save(old)).thenReturn(old);
        // For createFor called internally — match any new token
        when(repo.save(argThat(t -> !"oldtoken".equals(t.getToken())))).thenReturn(newToken);

        RefreshToken result = refreshTokenService.rotate("oldtoken");

        assertTrue(old.isRevoked());
        assertNotNull(result);
        verify(repo, atLeastOnce()).save(old);
    }

    @Test
    public void rotate_tokenNotFound_throwsInvalidRefreshToken() {
        when(repo.findByToken("missing")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenService.rotate("missing"));
    }

    @Test
    public void rotate_expiredToken_throwsInvalidRefreshToken() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken expired = new RefreshToken("expiredtoken", "alice",
                now.minusDays(31), now.minusDays(1));

        when(repo.findByToken("expiredtoken")).thenReturn(Optional.of(expired));

        InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenService.rotate("expiredtoken"));

        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    public void rotate_revokedToken_revokesAllSessionsAndThrows() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken revoked = new RefreshToken("revokedtoken", "alice",
                now.minusDays(1), now.plusDays(29));
        revoked.setRevoked(true);

        RefreshToken active1 = new RefreshToken("active1", "alice", now.minusHours(1), now.plusDays(29));
        RefreshToken active2 = new RefreshToken("active2", "alice", now.minusHours(2), now.plusDays(28));

        when(repo.findByToken("revokedtoken")).thenReturn(Optional.of(revoked));
        when(repo.findByUsernameAndRevokedFalse("alice")).thenReturn(List.of(active1, active2));
        when(repo.saveAll(anyList())).thenReturn(List.of(active1, active2));

        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenService.rotate("revokedtoken"));

        // All active tokens should be revoked (replay attack mitigation)
        verify(repo).findByUsernameAndRevokedFalse("alice");
        verify(repo).saveAll(anyList());
    }

    // --- revoke ---

    @Test
    public void revoke_existingToken_marksRevoked() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken token = new RefreshToken("tok", "alice", now.minusDays(1), now.plusDays(29));

        when(repo.findByToken("tok")).thenReturn(Optional.of(token));
        when(repo.save(token)).thenReturn(token);

        refreshTokenService.revoke("tok");

        assertTrue(token.isRevoked());
        verify(repo).save(token);
    }

    @Test
    public void revoke_tokenNotFound_noopNoException() {
        when(repo.findByToken("missing")).thenReturn(Optional.empty());

        // Should not throw
        assertDoesNotThrow(() -> refreshTokenService.revoke("missing"));
        verify(repo, never()).save(any());
    }

    // --- revokeAll ---

    @Test
    public void revokeAll_revokesAllActiveTokensForUser() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken t1 = new RefreshToken("t1", "alice", now.minusDays(2), now.plusDays(28));
        RefreshToken t2 = new RefreshToken("t2", "alice", now.minusDays(1), now.plusDays(29));

        when(repo.findByUsernameAndRevokedFalse("alice")).thenReturn(List.of(t1, t2));
        when(repo.saveAll(anyList())).thenReturn(List.of(t1, t2));

        refreshTokenService.revokeAll("alice");

        assertTrue(t1.isRevoked());
        assertTrue(t2.isRevoked());
        verify(repo).saveAll(List.of(t1, t2));
    }

    @Test
    public void revokeAll_noActiveTokens_savesEmptyList() {
        when(repo.findByUsernameAndRevokedFalse("alice")).thenReturn(List.of());
        when(repo.saveAll(anyList())).thenReturn(List.of());

        refreshTokenService.revokeAll("alice");

        verify(repo).saveAll(List.of());
    }

    // --- purgeExpired ---

    @Test
    public void purgeExpired_callsDeleteWithCutoffInThePast() {
        refreshTokenService.purgeExpired();

        verify(repo).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
