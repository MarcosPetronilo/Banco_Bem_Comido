package com.example.BemComido.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.BemComido.model.Receita;
import com.example.BemComido.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReceitaRepository extends JpaRepository<Receita, Long>, JpaSpecificationExecutor<Receita> {
	Page<Receita> findByAutor(Usuario autor, Pageable pageable);
	Page<Receita> findByAutorUsernameIgnoreCase(String username, Pageable pageable);
}
