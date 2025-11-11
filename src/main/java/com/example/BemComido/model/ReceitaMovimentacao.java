package com.example.BemComido.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "receita_movimentacoes")
public class ReceitaMovimentacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovimentacaoTipo tipo;

    @Column(name = "receita_id", nullable = false)
    private Long receitaId;

    @Column(name = "receita_nome", nullable = false)
    private String receitaNome;

    @Column(name = "autor_id")
    private Long autorId;

    @Column(name = "autor_username")
    private String autorUsername;

    @Column(length = 1000)
    private String justificativa;

    @Column(name = "momento", nullable = false)
    private Instant momento;

    @PrePersist
    public void prePersist() {
        if (momento == null) momento = Instant.now();
    }

    public ReceitaMovimentacao() {}

    public ReceitaMovimentacao(MovimentacaoTipo tipo, Long receitaId, String receitaNome, Long autorId, String autorUsername, String justificativa) {
        this.tipo = tipo;
        this.receitaId = receitaId;
        this.receitaNome = receitaNome;
        this.autorId = autorId;
        this.autorUsername = autorUsername;
        this.justificativa = justificativa;
        this.momento = Instant.now();
    }

    public Long getId() { return id; }
    public MovimentacaoTipo getTipo() { return tipo; }
    public void setTipo(MovimentacaoTipo tipo) { this.tipo = tipo; }
    public Long getReceitaId() { return receitaId; }
    public void setReceitaId(Long receitaId) { this.receitaId = receitaId; }
    public String getReceitaNome() { return receitaNome; }
    public void setReceitaNome(String receitaNome) { this.receitaNome = receitaNome; }
    public Long getAutorId() { return autorId; }
    public void setAutorId(Long autorId) { this.autorId = autorId; }
    public String getAutorUsername() { return autorUsername; }
    public void setAutorUsername(String autorUsername) { this.autorUsername = autorUsername; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    public Instant getMomento() { return momento; }
    public void setMomento(Instant momento) { this.momento = momento; }
}
