package com.example.BemComido.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
// imports for collections removed because foto/tags moved to frontend

@Entity
@Table(name = "receitas")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String nome;

    @NotBlank
    @Lob
    @Column(nullable = false)
    private String ingredientes;

    @NotBlank
    @Lob
    @Column(nullable = false, name = "modo_preparo")
    private String modoPreparo;


    @NotNull
    @Column(nullable = false, name = "tempo_medio_min")
    private Integer tempoMedioMinutos;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String rendimento; // exemplo: "4 porções"

    // foto e tags são gerenciadas pelo frontend; backend mantém apenas dados essenciais

    @Embedded
    private InformacoesNutricionais infoNutricional; // opcional

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "nivel_tecnica", length = 20)
    private NivelTecnica nivelTecnica;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }
    public Usuario getAutor() { return autor; }
    public String getNome() { return nome; }
    public String getIngredientes() { return ingredientes; }
    public String getModoPreparo() { return modoPreparo; }
    public Integer getTempoMedioMinutos() { return tempoMedioMinutos; }
    public String getRendimento() { return rendimento; }
    
    public InformacoesNutricionais getInfoNutricional() { return infoNutricional; }
    public NivelTecnica getNivelTecnica() { return nivelTecnica; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setAutor(Usuario autor) { this.autor = autor; }
    public void setNome(String nome) { this.nome = nome; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }
    public void setModoPreparo(String modoPreparo) { this.modoPreparo = modoPreparo; }
    public void setTempoMedioMinutos(Integer tempoMedioMinutos) { this.tempoMedioMinutos = tempoMedioMinutos; }
    public void setRendimento(String rendimento) { this.rendimento = rendimento; }
    public void setInfoNutricional(InformacoesNutricionais infoNutricional) { this.infoNutricional = infoNutricional; }
    public void setNivelTecnica(NivelTecnica nivelTecnica) { this.nivelTecnica = nivelTecnica; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
