package com.example.BemComido.repository;

import com.example.BemComido.model.ReceitaMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReceitaMovimentacaoRepository extends JpaRepository<ReceitaMovimentacao, Long> {
    @Query("SELECT m FROM ReceitaMovimentacao m WHERE (:from IS NULL OR m.momento >= :from) AND (:to IS NULL OR m.momento <= :to) ORDER BY m.momento DESC")
    List<ReceitaMovimentacao> findMovimentacoes(@Param("from") Instant from, @Param("to") Instant to);
}
