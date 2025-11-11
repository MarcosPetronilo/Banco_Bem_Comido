package com.example.BemComido.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class InformacoesNutricionais {
    private Integer calorias;      // kcal por porção
    private Double proteinas;      // g
    private Double carboidratos;   // g
    private Double gorduras;       // g
    private Double fibras;         // g
    private Double sodio;          // mg

    public Integer getCalorias() { return calorias; }
    public void setCalorias(Integer calorias) { this.calorias = calorias; }
    public Double getProteinas() { return proteinas; }
    public void setProteinas(Double proteinas) { this.proteinas = proteinas; }
    public Double getCarboidratos() { return carboidratos; }
    public void setCarboidratos(Double carboidratos) { this.carboidratos = carboidratos; }
    public Double getGorduras() { return gorduras; }
    public void setGorduras(Double gorduras) { this.gorduras = gorduras; }
    public Double getFibras() { return fibras; }
    public void setFibras(Double fibras) { this.fibras = fibras; }
    public Double getSodio() { return sodio; }
    public void setSodio(Double sodio) { this.sodio = sodio; }
}
