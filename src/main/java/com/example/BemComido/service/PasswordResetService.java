package com.example.BemComido.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BemComido.model.PasswordResetToken;
import com.example.BemComido.model.Usuario;
import com.example.BemComido.repository.PasswordResetTokenRepository;
import com.example.BemComido.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;

    public PasswordResetService(UsuarioRepository usuarioRepository, PasswordResetTokenRepository tokenRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    // Gera e envia um código numérico de 6 dígitos (one-time, 10 minutos por padrão)
    public void requestPasswordResetCode(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) return;
        Usuario usuario = usuarioOpt.get();

        // Gera código/token curto e armazena HASH + expiração diretamente no usuário
        String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        String codeHash = sha256Hex(code);
        usuario.setResetPasswordToken(codeHash);
        usuario.setResetPasswordTokenExpires(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

    String subject = "Recuperação de Senha";
    String body = "Olá,\n\nSeu código de verificação é: " + code + "\n" +
        "Ele expira em 15 minutos.\n\nSe não foi você, ignore este e-mail.\n\nEquipe Bem Comido";
    // Força remetente padrão solicitado
    String from = "ra122785@uem.br";
        try {
            emailService.sendPlainText(usuario.getEmail(), subject, body, from);
        } catch (Exception ex) {
            System.out.println("[DEV] Falha ao enviar email real. Código seria enviado para " + usuario.getEmail() + ": " + code);
        }
    }

    public boolean verifyCodeAndResetPassword(String email, String code, String newPassword) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) return false;
        Usuario usuario = usuarioOpt.get();

        String codeHash = sha256Hex(code);
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findTopByUsuarioIdAndTokenHashAndUsedAtIsNullOrderByCreatedAtDesc(usuario.getId(), codeHash);
        if (tokenOpt.isEmpty()) return false;
        PasswordResetToken token = tokenOpt.get();
        if (!"CODE".equals(token.getTokenType())) return false;
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) return false;

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        return true;
    }

    // Valida o código sem consumi-lo (não marca como usado e não altera senha)
    public boolean isCodeValid(String email, String code) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) return false;
        Usuario usuario = usuarioOpt.get();

        String codeHash = sha256Hex(code);
        Optional<PasswordResetToken> tokenOpt = tokenRepository
                .findTopByUsuarioIdAndTokenHashAndUsedAtIsNullOrderByCreatedAtDesc(usuario.getId(), codeHash);
        if (tokenOpt.isEmpty()) return false;
        PasswordResetToken token = tokenOpt.get();
        if (!"CODE".equals(token.getTokenType())) return false;
        return !token.getExpiresAt().isBefore(LocalDateTime.now());
    }

    // Valida o código e gera um sessionToken para concluir a mudança de senha sem precisar reenviar email e código
    public Optional<String> validateCodeAndStartSession(String email, String code) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) return Optional.empty();
        Usuario usuario = usuarioOpt.get();

        String codeHash = sha256Hex(code);
        Optional<PasswordResetToken> tokenOpt = tokenRepository
                .findTopByUsuarioIdAndTokenHashAndUsedAtIsNullOrderByCreatedAtDesc(usuario.getId(), codeHash);
        if (tokenOpt.isEmpty()) return Optional.empty();

        PasswordResetToken token = tokenOpt.get();
        if (!"CODE".equals(token.getTokenType())) return Optional.empty();
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) return Optional.empty();

        // Gera sessionToken curto (ex.: 15 minutos) e NÃO marca o código como usado ainda
        String session = java.util.UUID.randomUUID().toString().replace("-", "");
        token.setSessionToken(session);
        token.setSessionExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);
        return Optional.of(session);
    }

    // Versão sem email: valida pelo hash do código e gera sessão
    public Optional<String> validateCodeAndStartSession(String code) {
        String codeHash = sha256Hex(code);
        Optional<PasswordResetToken> tokenOpt = tokenRepository
                .findTopByTokenHashAndUsedAtIsNullOrderByCreatedAtDesc(codeHash);
        if (tokenOpt.isEmpty()) return Optional.empty();
        PasswordResetToken token = tokenOpt.get();
        if (!"CODE".equals(token.getTokenType())) return Optional.empty();
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) return Optional.empty();
        String session = java.util.UUID.randomUUID().toString().replace("-", "");
        token.setSessionToken(session);
        token.setSessionExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);
        return Optional.of(session);
    }

    // Conclui a alteração de senha apenas com sessionToken e nova senha (consome o código e invalida a sessão)
    public boolean completeResetWithSession(String sessionToken, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findBySessionToken(sessionToken);
        if (tokenOpt.isEmpty()) return false;
        PasswordResetToken token = tokenOpt.get();
        if (token.getUsedAt() != null) return false;
        if (token.getSessionExpiresAt() == null || token.getSessionExpiresAt().isBefore(LocalDateTime.now())) return false;

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(token.getUsuarioId());
        if (usuarioOpt.isEmpty()) return false;

        Usuario usuario = usuarioOpt.get();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        token.setUsedAt(LocalDateTime.now());
        token.setSessionToken(null);
        token.setSessionExpiresAt(null);
        tokenRepository.save(token);
        return true;
    }

    // Fluxo por link removido. Utilize verifyCodeAndResetPassword.

    // Novo fluxo: usa o token salvo no usuário (hash + expiração)
    public boolean resetPasswordUsingUserToken(String rawToken, String newPassword) {
        String hash = sha256Hex(rawToken);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetPasswordToken(hash);
        if (usuarioOpt.isEmpty()) return false;
        Usuario usuario = usuarioOpt.get();
        if (usuario.getResetPasswordTokenExpires() == null || usuario.getResetPasswordTokenExpires().isBefore(LocalDateTime.now())) {
            return false;
        }
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpires(null);
        usuarioRepository.save(usuario);
        return true;
    }

    @Transactional
    public void updatePasswordByUserIdAndClearResetToken(Long userId, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (usuario.getResetPasswordToken() == null || usuario.getResetPasswordTokenExpires() == null
                || usuario.getResetPasswordTokenExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código expirado. Gere um novo.");
        }
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpires(null);
        usuarioRepository.save(usuario);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }

    // Fluxo por link removido.
}
