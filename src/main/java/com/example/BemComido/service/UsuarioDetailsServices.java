package com.example.BemComido.service;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.BemComido.model.Usuario;
import com.example.BemComido.repository.UsuarioRepository;
@Service
public class UsuarioDetailsServices implements UserDetailsService {



    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsServices(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com username: " + username));

        // Obs.: a senha precisa estar codificada (ex.: BCrypt). Se ainda estiver em texto puro em dev,
        // você pode usar temporariamente {noop} no prefixo ou configurar um PasswordEncoder.
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles("USER") // ajuste roles conforme necessário
                .build();
    }
    
}
