package com.example.BemComido.repository;

import com.example.BemComido.model.UserMovimentacao;
import com.example.BemComido.model.UserMovimentacaoTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface UserMovimentacaoRepository extends JpaRepository<UserMovimentacao, Long> {
    @Query("SELECT m FROM UserMovimentacao m WHERE (:from IS NULL OR m.momento >= :from) AND (:to IS NULL OR m.momento <= :to) ORDER BY m.momento DESC")
    List<UserMovimentacao> findMovimentacoes(@Param("from") Instant from, @Param("to") Instant to);
    long countByTipo(UserMovimentacaoTipo tipo);
}
