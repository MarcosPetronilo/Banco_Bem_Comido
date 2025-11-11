package com.example.BemComido.repository;

import com.example.BemComido.model.deleted.ReceitaExcluida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceitaExcluidaRepository extends JpaRepository<ReceitaExcluida, Long> {
    long countBy();
}
