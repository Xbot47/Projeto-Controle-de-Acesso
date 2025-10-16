package com.api.apitest.dto;

import java.time.LocalDateTime;

public class HistoricoCompletoDTO {
    private String documentoVisitante;
    private String nomeVisitante;
    private String sobrenomeVisitante;
    private String CategoriaVisitante;
    private LocalDateTime dataHoraEntrada;
    private String nomeSetorVisitado;
    private String nomeUnidadeVisitado;
    private String nomeVisitado;
    private String sobrenomeVisitado;

    // Getters e Setters
    public String getDocumentoVisitante() { return documentoVisitante; }
    public void setDocumentoVisitante(String documentoVisitante) { this.documentoVisitante = documentoVisitante; }

    public String getNomeVisitante() { return nomeVisitante; }
    public void setNomeVisitante(String nomeVisitante) { this.nomeVisitante = nomeVisitante; }

    public String getSobrenomeVisitante() { return sobrenomeVisitante; }
    public void setSobrenomeVisitante(String sobrenomeVisitante) { this.sobrenomeVisitante = sobrenomeVisitante; }

    public String getNomeCategoriaVisitante() { return CategoriaVisitante; }
    public void setNomeCategoriaVisitante(String nomeCategoriaVisitante) { this.CategoriaVisitante = nomeCategoriaVisitante; }

    public LocalDateTime getDataHoraEntrada() { return dataHoraEntrada; }
    public void setDataHoraEntrada(LocalDateTime dataHoraEntrada) { this.dataHoraEntrada = dataHoraEntrada; }

    public String getNomeSetorVisitado() { return nomeSetorVisitado; }
    public void setNomeSetorVisitado(String nomeSetorVisitado) { this.nomeSetorVisitado = nomeSetorVisitado; }

    public String getNomeUnidadeVisitado() { return nomeUnidadeVisitado; }
    public void setNomeUnidadeVisitado(String nomeUnidadeVisitado) { this.nomeUnidadeVisitado = nomeUnidadeVisitado; }

    public String getNomeVisitado() { return nomeVisitado; }
    public void setNomeVisitado(String nomeVisitado) { this.nomeVisitado = nomeVisitado; }

    public String getSobrenomeVisitado() { return sobrenomeVisitado; }
    public void setSobrenomeVisitado(String sobrenomeVisitado) { this.sobrenomeVisitado = sobrenomeVisitado; }
}