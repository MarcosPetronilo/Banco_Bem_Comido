package com.example.BemComido.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "usuarios_favoritos",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_fav_owner_target", columnNames = {"owner_id", "target_id"}))
public class UsuarioFavorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Usuario owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private Usuario target;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public UsuarioFavorito() {}

    public UsuarioFavorito(Usuario owner, Usuario target) {
        this.owner = owner;
        this.target = target;
        this.criadoEm = Instant.now();
    }

    public Long getId() { return id; }
    public Usuario getOwner() { return owner; }
    public Usuario getTarget() { return target; }
    public Instant getCriadoEm() { return criadoEm; }

    public void setOwner(Usuario owner) { this.owner = owner; }
    public void setTarget(Usuario target) { this.target = target; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
