package com.example.BemComido.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // SHA-256 hex string (64 chars)
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;
    // Opcional: diferenciar formatos (LINK ou CODE). Mantemos compatibilidade.
    @Column(name = "token_type", nullable = false, length = 10)
    private String tokenType = "LINK";

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Sessão temporária criada após validação do código, para concluir a troca de senha
    @Column(name = "session_token", length = 128, unique = true)
    private String sessionToken;

    @Column(name = "session_expires_at")
    private LocalDateTime sessionExpiresAt;

    public PasswordResetToken() {}

    public PasswordResetToken(Long usuarioId, String tokenHash, LocalDateTime expiresAt) {
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getSessionToken() { return sessionToken; }
    public LocalDateTime getSessionExpiresAt() { return sessionExpiresAt; }

    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    public void setSessionExpiresAt(LocalDateTime sessionExpiresAt) { this.sessionExpiresAt = sessionExpiresAt; }
}
