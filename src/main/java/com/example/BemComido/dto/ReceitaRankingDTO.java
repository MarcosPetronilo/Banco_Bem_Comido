package com.example.BemComido.dto;

import com.example.BemComido.model.Receita;

public class ReceitaRankingDTO {
    private final Receita receita;
    private final Double media;
    private final Long quantidade;

    public ReceitaRankingDTO(Receita receita, Double media, Long quantidade) {
        this.receita = receita;
        this.media = media;
        this.quantidade = quantidade;
    }

    public Receita getReceita() { return receita; }
    public Double getMedia() { return media; }
    public Long getQuantidade() { return quantidade; }
}
