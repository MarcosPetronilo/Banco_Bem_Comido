package com.example.BemComido.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BemComido.model.Usuario;
import com.example.BemComido.repository.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import com.example.BemComido.model.UserMovimentacao;
import com.example.BemComido.model.UserMovimentacaoTipo;
import com.example.BemComido.repository.UserMovimentacaoRepository;
import org.springframework.security.core.Authentication;

@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMovimentacaoRepository userMovimentacaoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, UserMovimentacaoRepository userMovimentacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.userMovimentacaoRepository = userMovimentacaoRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Usuario registrarUsuario(String username, String password, String email,
                                    java.time.LocalDate dataNascimento, String pais, String telefone) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já em uso");
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nome já em uso");
        }
        String senhaCriptografada = passwordEncoder.encode(password);
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(senhaCriptografada);
        usuario.setEmail(email);
        usuario.setDataNascimento(dataNascimento);
        usuario.setPais(pais);
        // telefone opcional: usar vazio para compatibilidade com esquemas legados NOT NULL
        usuario.setTelefone(telefone == null ? "" : telefone.trim());
        usuario.setRole(com.example.BemComido.model.Role.USER);
        try {
            Usuario saved = usuarioRepository.save(usuario);
            // log movimentação de criação de usuário
            userMovimentacaoRepository.save(new UserMovimentacao(UserMovimentacaoTipo.CREATED,
                    saved.getId(), saved.getUsername(), null, null, null));
            return saved;
        } catch (DataIntegrityViolationException e) {
            // Fallback caso alguma constraint de banco dispare
            throw new IllegalArgumentException("Violação de integridade: " + e.getMostSpecificCause().getMessage());
        }
    }

    public Usuario registrarAdministrador(String username, String password, String email,
                                          java.time.LocalDate dataNascimento, String pais, String telefone) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já em uso");
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nome já em uso");
        }
        String senhaCriptografada = passwordEncoder.encode(password);
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(senhaCriptografada);
        usuario.setEmail(email);
        usuario.setDataNascimento(dataNascimento);
        usuario.setPais(pais);
        // telefone opcional: usar vazio para compatibilidade com esquemas legados NOT NULL
        usuario.setTelefone(telefone == null ? "" : telefone.trim());
        usuario.setRole(com.example.BemComido.model.Role.ADMIN);
        try {
            Usuario saved = usuarioRepository.save(usuario);
            userMovimentacaoRepository.save(new UserMovimentacao(UserMovimentacaoTipo.CREATED,
                    saved.getId(), saved.getUsername(), null, null, "admin-created"));
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Violação de integridade: " + e.getMostSpecificCause().getMessage());
        }
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> buscarPorUsernameParcial(String usernameFragment) {
        if (usernameFragment == null || usernameFragment.trim().isEmpty()) {
            return List.of();
        }
        return usuarioRepository.findByUsernameContainingIgnoreCase(usernameFragment.trim());
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public boolean deletar(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deletarMinhaConta(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Não autenticado");
        }
        String subject = auth.getName();
        Optional<Usuario> opt = usuarioRepository.findByUsername(subject);
        if (opt.isEmpty()) opt = usuarioRepository.findByEmail(subject);
        if (opt.isEmpty()) throw new IllegalArgumentException("Usuário autenticado não encontrado");
        Usuario user = opt.get();
        Long uid = user.getId();
        usuarioRepository.deleteById(uid);
        userMovimentacaoRepository.save(new UserMovimentacao(UserMovimentacaoTipo.DELETED,
                uid, user.getUsername(), uid, user.getUsername(), "self-delete"));
        return true;
    }

    @Transactional
    public boolean deletarPorAdmin(Long id, String justificativa, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Não autenticado");
        }
        String subject = auth.getName();
        Usuario admin = usuarioRepository.findByUsername(subject).orElseGet(() -> usuarioRepository.findByEmail(subject).orElse(null));
        if (admin == null || admin.getRole() == null || admin.getRole() == com.example.BemComido.model.Role.USER) {
            throw new IllegalArgumentException("Apenas administradores podem excluir usuários");
        }
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return false;
        Usuario alvo = opt.get();
        usuarioRepository.deleteById(alvo.getId());
        userMovimentacaoRepository.save(new UserMovimentacao(UserMovimentacaoTipo.DELETED,
                alvo.getId(), alvo.getUsername(), admin.getId(), admin.getUsername(), justificativa));
        return true;
    }

    @Transactional
    public Usuario promoverParaAdmin(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Identificador vazio");
        }
        String idf = identifier.trim();
        Optional<Usuario> userOpt = usuarioRepository.findByUsername(idf);
        if (userOpt.isEmpty()) {
            userOpt = usuarioRepository.findByEmail(idf);
        }
        Usuario user = userOpt.orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (user.getRole() != com.example.BemComido.model.Role.ADMIN) {
            user.setRole(com.example.BemComido.model.Role.ADMIN);
            user = usuarioRepository.save(user);
            userMovimentacaoRepository.save(new UserMovimentacao(UserMovimentacaoTipo.PROMOTED,
                    user.getId(), user.getUsername(), null, null, null));
        }
        return user;
    }
}
