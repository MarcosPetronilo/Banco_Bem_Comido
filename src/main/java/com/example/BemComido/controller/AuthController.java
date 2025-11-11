package com.example.BemComido.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
 
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
 
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
 
import org.springframework.web.bind.annotation.RestController;

import com.example.BemComido.model.Usuario;
import com.example.BemComido.repository.UsuarioRepository;
import com.example.BemComido.security.JwtService;
import com.example.BemComido.service.PasswordResetService;
import com.example.BemComido.service.UsuarioService;

import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.security.access.prepost.PreAuthorize;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioService usuarioService, PasswordEncoder passwordEncoder, PasswordResetService passwordResetService, JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    // DTO interno para registro
    public static class RegisterRequest {
        @NotBlank(message = "Nome é obrigatório")
        public String username;
        @NotBlank(message = "Senha é obrigatória")
        public String password;
        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        public String email;
        @NotBlank(message = "País é obrigatório")
        public String pais;
        @NotBlank(message = "Data de nascimento é obrigatória")
        public String dataNascimento; // formato ISO yyyy-MM-dd
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Telefone deve conter apenas dígitos (10-15) e opcional + no início")
        public String telefone; // opcional
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        java.time.LocalDate dob;
        try {
            dob = java.time.LocalDate.parse(request.dataNascimento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de dataNascimento inválido. Use yyyy-MM-dd"));
        }
        Usuario usuario = usuarioService.registrarUsuario(
            request.username,
            request.password,
            request.email,
            dob,
            request.pais,
            request.telefone
        );
        return ResponseEntity.ok(usuario);
    }

    public static class AdminRegisterRequest extends RegisterRequest { }

    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminRegisterRequest request, @RequestHeader(name = "Authorization", required = false) String authorization) {
        // Como há @PreAuthorize, a checagem manual abaixo seria redundante; manteremos somente a validação do payload
        java.time.LocalDate dob;
        try {
            dob = java.time.LocalDate.parse(request.dataNascimento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de dataNascimento inválido. Use yyyy-MM-dd"));
        }
        Usuario novoAdmin = usuarioService.registrarAdministrador(
            request.username,
            request.password,
            request.email,
            dob,
            request.pais,
            request.telefone
        );
        return ResponseEntity.ok(novoAdmin);
    }

    // Promover usuário existente a ADMIN (restrito a ADMIN)
    @PostMapping("/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> promoteUser(@RequestBody Map<String, String> body) {
        String identifier = null;
        if (body != null) {
            String u = body.get("username");
            String e = body.get("email");
            if (u != null && !u.isBlank()) identifier = u.trim();
            else if (e != null && !e.isBlank()) identifier = e.trim();
        }
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Informe username ou email"));
        }
        try {
            Usuario adminizado = usuarioService.promoverParaAdmin(identifier);
            return ResponseEntity.ok(Map.of(
                "id", adminizado.getId(),
                "username", adminizado.getUsername(),
                "email", adminizado.getEmail(),
                "role", adminizado.getRole().name()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        }
    }

    public static class LoginRequest {
        // Enviando username OU email
        public String username;
        @Email(message = "Email inválido")
        public String email;
        @NotBlank(message = "Password é obrigatório")
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        String username = request.username != null ? request.username.trim() : null;
        String email = request.email != null ? request.email.trim() : null;

        if ((username == null || username.isBlank()) && (email == null || email.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Envie username ou email, além de password"));
        }

        Optional<Usuario> usuario = Optional.empty();
        if (username != null && !username.isBlank()) {
            usuario = usuarioService.buscarPorUsername(username);
            if (usuario.isEmpty()) {
                usuario = usuarioService.buscarPorEmail(username);
            }
        }
        if (usuario.isEmpty() && email != null && !email.isBlank()) {
            usuario = usuarioService.buscarPorEmail(email);
        }

        if (usuario.isPresent() && passwordEncoder.matches(request.password, usuario.get().getPassword())) {
            String role = usuario.get().getRole() != null ? usuario.get().getRole().name() : "USER";
            String subject = (usuario.get().getUsername() != null && !usuario.get().getUsername().isBlank())
                    ? usuario.get().getUsername()
                    : usuario.get().getEmail();
            String token = jwtService.generateToken(subject, Map.of("role", role), 24 * 60 * 60 * 1000L); // 24 horas
            return ResponseEntity.ok(Map.of("token", token, "role", role));
        }
        return ResponseEntity.status(401).body("Credenciais inválidas");
    }

    // DTOs para recuperação de senha
    public static class ForgotPasswordRequest {
        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        public String email;
        // Para fluxo por código não é necessário resetBaseUrl
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        // Validar se email está cadastrado; se não, retornar 404
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(req.email);
        if (usuario.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Email não cadastrado"));
        }
        // Enviar código de 6 dígitos em vez de link
        passwordResetService.requestPasswordResetCode(req.email);
        return ResponseEntity.ok(Map.of("message", "Código de verificação enviado para o email."));
    }

    // ==== Fluxo por código de 6 dígitos ====
    public static class ForgotPasswordCodeRequest {
        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        public String email;
    }

    @PostMapping("/forgot-password/code")
    public ResponseEntity<?> forgotPasswordByCode(@Valid @RequestBody ForgotPasswordCodeRequest req) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(req.email);
        if (usuario.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Email não cadastrado"));
        }
        passwordResetService.requestPasswordResetCode(req.email);
        return ResponseEntity.ok(Map.of("message", "Código de verificação enviado para o email."));
    }

    // Endpoint /reset-password/code removido (fluxo descontinuado)

    // Fluxo por link removido: somente código de 6 dígitos permanece.
    
   
    public static class VerifyCodeRequest {
        @NotBlank(message = "Código é obrigatório")
        public String code;
    }

    public static class UpdatePasswordRequest {
        @NotBlank(message = "Nova senha é obrigatória")
        public String novaSenha;
        @NotBlank(message = "Confirmação de senha é obrigatória")
        public String confirmarSenha;
    }

    // POST /auth/verify-code
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest req) {
        String hash = sha256Hex(req.code);
        Optional<Usuario> userOpt = usuarioRepository.findByResetPasswordToken(hash);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido ou expirado."));
        }
        Usuario user = userOpt.get();
        LocalDateTime expAt = user.getResetPasswordTokenExpires();
        if (expAt == null || expAt.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido ou expirado."));
        }
        // Gera token de 5 minutos com role PASSWORD_RESET e uid
        long fiveMinMs = 5 * 60 * 1000L;
        String token = jwtService.generateToken(
                "password-reset",
                Map.of("uid", user.getId(), "role", "PASSWORD_RESET"),
                fiveMinMs
        );
        return ResponseEntity.ok(Map.of(
                "token", token,
                "expiresInSeconds", 300
        ));
    }

    // POST /auth/update-password (Authorization: Bearer <token>)
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest req,
                                            @RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Credenciais ausentes."));
        }
        String token = authorization.substring("Bearer ".length());
        Jws<Claims> jws;
        try {
            jws = jwtService.parse(token);
        } catch (JwtException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "Token inválido ou expirado."));
        }
        Claims claims = jws.getBody();
        if (!"password-reset".equals(claims.getSubject())) {
            return ResponseEntity.status(401).body(Map.of("message", "Token inválido."));
        }
        Object role = claims.get("role");
        if (role == null || !"PASSWORD_RESET".equals(role.toString())) {
            return ResponseEntity.status(401).body(Map.of("message", "Token sem permissão."));
        }
        Object uidObj = claims.get("uid");
        if (uidObj == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Token inválido."));
        }
        Long uid;
        try {
            uid = Long.valueOf(uidObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Token inválido."));
        }

        if (!req.novaSenha.equals(req.confirmarSenha)) {
            return ResponseEntity.badRequest().body(Map.of("message", "As senhas não conferem."));
        }

        Optional<Usuario> userOpt = usuarioRepository.findById(uid);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Usuário não encontrado."));
        }
        Usuario user = userOpt.get();
        // Opcional: garantir que ainda existe um reset token válido no usuário
        if (user.getResetPasswordToken() == null || user.getResetPasswordTokenExpires() == null || user.getResetPasswordTokenExpires().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código expirado. Gere um novo."));
        }

        user.setPassword(passwordEncoder.encode(req.novaSenha));
        // invalida o token para não ser reaproveitado
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpires(null);
        usuarioRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Senha atualizada com sucesso."));
    }

    // Utilitário local: SHA-256 em hex (para hashear o código)
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
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
    // Endpoint /reset-password removido (fluxo descontinuado)
    
}
