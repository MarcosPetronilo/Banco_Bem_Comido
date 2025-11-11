package com.example.BemComido.repository;

import com.example.BemComido.model.ReceitaFavorita;
import com.example.BemComido.model.Usuario;
import com.example.BemComido.model.Receita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface ReceitaFavoritaRepository extends JpaRepository<ReceitaFavorita, Long> {
    boolean existsByOwnerAndReceita(Usuario owner, Receita receita);
    Optional<ReceitaFavorita> findByOwnerAndReceita(Usuario owner, Receita receita);
    Page<ReceitaFavorita> findByOwner(Usuario owner, Pageable pageable);
}
