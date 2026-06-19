package com.eventmaster.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens",
       indexes = @Index(name = "idx_refresh_token_username", columnList = "username"))
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshToken() {}

    public RefreshToken(String token, String username, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.token = token;
        this.username = username;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
