package com.example.BemComido.repository;

import com.example.BemComido.model.UsuarioFavorito;
import com.example.BemComido.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface UsuarioFavoritoRepository extends JpaRepository<UsuarioFavorito, Long> {
    boolean existsByOwnerAndTarget(Usuario owner, Usuario target);
    Optional<UsuarioFavorito> findByOwnerAndTarget(Usuario owner, Usuario target);
    Page<UsuarioFavorito> findByOwner(Usuario owner, Pageable pageable);
}
