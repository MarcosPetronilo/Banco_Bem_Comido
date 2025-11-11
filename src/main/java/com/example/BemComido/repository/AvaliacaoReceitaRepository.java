package com.example.BemComido.repository;

import com.example.BemComido.dto.ReceitaRankingDTO;
import com.example.BemComido.model.AvaliacaoReceita;
import com.example.BemComido.model.Receita;
import com.example.BemComido.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface AvaliacaoReceitaRepository extends JpaRepository<AvaliacaoReceita, Long> {

    Optional<AvaliacaoReceita> findByReceitaAndUsuario(Receita receita, Usuario usuario);

    @Query(value = "SELECT new com.example.BemComido.dto.ReceitaRankingDTO(a.receita, AVG(a.score), COUNT(a.id)) " +
            "FROM AvaliacaoReceita a " +
            "WHERE (:since IS NULL OR a.createdAt >= :since) " +
            "GROUP BY a.receita " +
            "ORDER BY AVG(a.score) DESC, COUNT(a.id) DESC",
            countQuery = "SELECT COUNT(DISTINCT a.receita) FROM AvaliacaoReceita a WHERE (:since IS NULL OR a.createdAt >= :since)")
    Page<ReceitaRankingDTO> findTopSince(@Param("since") Instant since, Pageable pageable);
}
