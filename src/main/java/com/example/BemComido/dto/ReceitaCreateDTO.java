package com.example.BemComido.dto;

import com.example.BemComido.model.InformacoesNutricionais;
import com.example.BemComido.model.NivelTecnica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReceitaCreateDTO {
    @NotBlank
    private String nome;
    @NotBlank
    private String ingredientes;
    @NotBlank
    private String modoPreparo;
    @NotNull
    private Integer tempoMedioMinutos;
    @NotBlank
    private String rendimento;
    private InformacoesNutricionais infoNutricional; // opcional
    @NotNull
    private NivelTecnica nivelTecnica;

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
}
