package com.example.BemComido.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.BemComido.model.Usuario;
import com.example.BemComido.repository.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;

@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
            return usuarioRepository.save(usuario);
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
            return usuarioRepository.save(usuario);
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
}
