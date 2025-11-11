package com.example.BemComido.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BemComido.model.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    Optional<PasswordResetToken> findTopByUsuarioIdAndTokenHashAndUsedAtIsNullOrderByCreatedAtDesc(Long usuarioId, String tokenHash);
    Optional<PasswordResetToken> findBySessionToken(String sessionToken);
    Optional<PasswordResetToken> findTopByTokenHashAndUsedAtIsNullOrderByCreatedAtDesc(String tokenHash);
}
