package com.example.BemComido.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "receitas_favoritas",
        uniqueConstraints = @UniqueConstraint(name = "uk_receita_fav_owner_receita", columnNames = {"owner_id", "receita_id"}))
public class ReceitaFavorita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Usuario owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "receita_id", nullable = false)
    private Receita receita;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public ReceitaFavorita() {}

    public ReceitaFavorita(Usuario owner, Receita receita) {
        this.owner = owner;
        this.receita = receita;
        this.criadoEm = Instant.now();
    }

    public Long getId() { return id; }
    public Usuario getOwner() { return owner; }
    public Receita getReceita() { return receita; }
    public Instant getCriadoEm() { return criadoEm; }

    public void setOwner(Usuario owner) { this.owner = owner; }
    public void setReceita(Receita receita) { this.receita = receita; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
