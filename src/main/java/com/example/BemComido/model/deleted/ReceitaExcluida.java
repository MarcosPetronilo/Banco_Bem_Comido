package com.example.BemComido.model.deleted;

import com.example.BemComido.model.InformacoesNutricionais;
import com.example.BemComido.model.NivelTecnica;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "receitas_excluidas")
public class ReceitaExcluida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID da receita original
    @Column(name = "original_id", nullable = false)
    private Long originalId;

    @Column(name = "autor_id")
    private Long autorId;

    @Column(name = "autor_username")
    private String autorUsername;

    @Column(nullable = false)
    private String nome;

    @Lob
    private String ingredientes;

    @Lob
    @Column(name = "modo_preparo")
    private String modoPreparo;

    @Column(name = "tempo_medio_min")
    private Integer tempoMedioMinutos;

    private String rendimento;

    @Embedded
    private InformacoesNutricionais infoNutricional;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_tecnica")
    private NivelTecnica nivelTecnica;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "removido_em", nullable = false)
    private Instant removidoEm;

    @Column(name = "removido_por_id", nullable = false)
    private Long removidoPorId;

    @Column(name = "removido_por_username", nullable = false)
    private String removidoPorUsername;

    @Column(length = 1000)
    private String justificativa;

    @PrePersist
    public void prePersist() {
        if (removidoEm == null) removidoEm = Instant.now();
    }

    public Long getId() { return id; }
    public Long getOriginalId() { return originalId; }
    public void setOriginalId(Long originalId) { this.originalId = originalId; }
    public Long getAutorId() { return autorId; }
    public void setAutorId(Long autorId) { this.autorId = autorId; }
    public String getAutorUsername() { return autorUsername; }
    public void setAutorUsername(String autorUsername) { this.autorUsername = autorUsername; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getIngredientes() { return ingredientes; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }
    public String getModoPreparo() { return modoPreparo; }
    public void setModoPreparo(String modoPreparo) { this.modoPreparo = modoPreparo; }
    public Integer getTempoMedioMinutos() { return tempoMedioMinutos; }
    public void setTempoMedioMinutos(Integer tempoMedioMinutos) { this.tempoMedioMinutos = tempoMedioMinutos; }
    public String getRendimento() { return rendimento; }
    public void setRendimento(String rendimento) { this.rendimento = rendimento; }
    public InformacoesNutricionais getInfoNutricional() { return infoNutricional; }
    public void setInfoNutricional(InformacoesNutricionais infoNutricional) { this.infoNutricional = infoNutricional; }
    public NivelTecnica getNivelTecnica() { return nivelTecnica; }
    public void setNivelTecnica(NivelTecnica nivelTecnica) { this.nivelTecnica = nivelTecnica; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public Instant getRemovidoEm() { return removidoEm; }
    public void setRemovidoEm(Instant removidoEm) { this.removidoEm = removidoEm; }
    public Long getRemovidoPorId() { return removidoPorId; }
    public void setRemovidoPorId(Long removidoPorId) { this.removidoPorId = removidoPorId; }
    public String getRemovidoPorUsername() { return removidoPorUsername; }
    public void setRemovidoPorUsername(String removidoPorUsername) { this.removidoPorUsername = removidoPorUsername; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
}
