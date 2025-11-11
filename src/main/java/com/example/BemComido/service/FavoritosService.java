package com.example.BemComido.service;

import com.example.BemComido.model.*;
import com.example.BemComido.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoritosService {

    private final UsuarioRepository usuarioRepository;
    private final ReceitaRepository receitaRepository;
    private final UsuarioFavoritoRepository usuarioFavoritoRepository;
    private final ReceitaFavoritaRepository receitaFavoritaRepository;

    public FavoritosService(UsuarioRepository usuarioRepository,
                            ReceitaRepository receitaRepository,
                            UsuarioFavoritoRepository usuarioFavoritoRepository,
                            ReceitaFavoritaRepository receitaFavoritaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
        this.usuarioFavoritoRepository = usuarioFavoritoRepository;
        this.receitaFavoritaRepository = receitaFavoritaRepository;
    }

    private Usuario resolveUsuarioAutenticado(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Não autenticado");
        }
        String subject = auth.getName();
        return usuarioRepository.findByUsername(subject)
                .orElseGet(() -> usuarioRepository.findByEmail(subject).orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado: " + subject)));
    }

    // Usuários favoritos
    @Transactional
    public UsuarioFavorito favoritarUsuario(String username, Authentication auth) {
        Usuario owner = resolveUsuarioAutenticado(auth);
        if (owner.getUsername().equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Não é possível favoritar a si mesmo");
        }
        Usuario target = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário alvo não encontrado"));
        return usuarioFavoritoRepository.findByOwnerAndTarget(owner, target)
                .orElseGet(() -> usuarioFavoritoRepository.save(new UsuarioFavorito(owner, target)));
    }

    @Transactional
    public void desfavoritarUsuario(String username, Authentication auth) {
        Usuario owner = resolveUsuarioAutenticado(auth);
        Usuario target = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário alvo não encontrado"));
        usuarioFavoritoRepository.findByOwnerAndTarget(owner, target).ifPresent(usuarioFavoritoRepository::delete);
    }

    public Page<Usuario> listarUsuariosFavoritos(Authentication auth, Pageable pageable) {
        Usuario owner = resolveUsuarioAutenticado(auth);
        return usuarioFavoritoRepository.findByOwner(owner, pageable)
                .map(UsuarioFavorito::getTarget);
    }

    // Receitas favoritas
    @Transactional
    public ReceitaFavorita favoritarReceita(Long receitaId, Authentication auth) {
        Usuario owner = resolveUsuarioAutenticado(auth);
        Receita receita = receitaRepository.findById(receitaId)
                .orElseThrow(() -> new IllegalArgumentException("Receita não encontrada"));
        return receitaFavoritaRepository.findByOwnerAndReceita(owner, receita)
                .orElseGet(() -> receitaFavoritaRepository.save(new ReceitaFavorita(owner, receita)));
    }

    @Transactional
    public void desfavoritarReceita(Long receitaId, Authentication auth) {
        Usuario owner = resolveUsuarioAutenticado(auth);
        Receita receita = receitaRepository.findById(receitaId)
                .orElseThrow(() -> new IllegalArgumentException("Receita não encontrada"));
        receitaFavoritaRepository.findByOwnerAndReceita(owner, receita).ifPresent(receitaFavoritaRepository::delete);
    }

    public Page<Receita> listarReceitasFavoritas(Authentication auth, Pageable pageable) {
        Usuario owner = resolveUsuarioAutenticado(auth);
        return receitaFavoritaRepository.findByOwner(owner, pageable)
                .map(ReceitaFavorita::getReceita);
    }

    // Listagens auxiliares
    public Page<Receita> listarReceitasPorUsuario(String username, Pageable pageable) {
        return receitaRepository.findByAutorUsernameIgnoreCase(username, pageable);
    }

    public Page<Receita> listarMinhasReceitas(Authentication auth, Pageable pageable) {
        Usuario owner = resolveUsuarioAutenticado(auth);
        return receitaRepository.findByAutor(owner, pageable);
    }
}
