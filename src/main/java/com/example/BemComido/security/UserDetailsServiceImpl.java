package com.example.BemComido.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.example.BemComido.model.Usuario;
import com.example.BemComido.repository.UsuarioRepository;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Aqui o parâmetro é o 'username' fornecido pelo fluxo de autenticação
        String username = email;
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com username: " + username));

        String roleName = usuario.getRole() != null ? usuario.getRole().name() : "USER";
        // garante prefixo ROLE_
        String granted = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return new org.springframework.security.core.userdetails.User(
            usuario.getUsername(),
            usuario.getPassword(),
            List.of(new SimpleGrantedAuthority(granted))
        );
    }
}
