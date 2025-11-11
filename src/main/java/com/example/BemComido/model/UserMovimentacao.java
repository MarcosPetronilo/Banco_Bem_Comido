package com.example.BemComido.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_movimentacoes")
public class UserMovimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserMovimentacaoTipo tipo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "actor_id")
    private Long actorId; // quem executou a ação (admin ou o próprio)

    @Column(name = "actor_username", length = 100)
    private String actorUsername;

    @Column(name = "justificativa")
    private String justificativa;

    @Column(name = "momento", nullable = false)
    private Instant momento = Instant.now();

    public UserMovimentacao() {}

    public UserMovimentacao(UserMovimentacaoTipo tipo, Long userId, String username, Long actorId, String actorUsername, String justificativa) {
        this.tipo = tipo;
        this.userId = userId;
        this.username = username;
        this.actorId = actorId;
        this.actorUsername = actorUsername;
        this.justificativa = justificativa;
        this.momento = Instant.now();
    }

    public Long getId() { return id; }
    public UserMovimentacaoTipo getTipo() { return tipo; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Long getActorId() { return actorId; }
    public String getActorUsername() { return actorUsername; }
    public String getJustificativa() { return justificativa; }
    public Instant getMomento() { return momento; }

    public void setTipo(UserMovimentacaoTipo tipo) { this.tipo = tipo; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    public void setMomento(Instant momento) { this.momento = momento; }
}
