package com.api.apitest.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "HistoricosVisitados")
@IdClass(HistoricoVisitadoId.class)
public class HistoricoVisitado {

    @Id
    @Column(name = "DocumentoVisitado", length = 30)
    private String documentoVisitado;

    @Id
    @Column(name = "Codigo_Historicos")
    private Long codigoHistoricos;

    @Column(name = "CodigoParticaoVisitado", nullable = false)
    private Integer codigoParticaoVisitado = 1;

    @Column(name = "NomeParticaoVisitado", length = 30, nullable = false)
    private String nomeParticaoVisitado = "Principal";

    @Column(name = "CodigoUnidadeVisitado", nullable = false)
    private Integer codigoUnidadeVisitado = 1;

    @Column(name = "NomeUnidadeVisitado", length = 30, nullable = false)
    private String nomeUnidadeVisitado;

    @Column(name = "CodigoSetorVisitado", nullable = false)
    private Integer codigoSetorVisitado = 1;

    @Column(name = "NomeSetorVisitado", length = 30, nullable = false)
    private String nomeSetorVisitado;

    @Column(name = "NomeVisitado", length = 60, nullable = false)
    private String nomeVisitado;

    @Column(name = "SobreNomeVisitado", length = 30)
    private String sobrenomeVisitado;

    @Column(name = "VisitadoPrincipal")
    private Boolean visitadoPrincipal = false;

    @Column(name = "MensagemVisitado", length = 255)
    private String mensagemVisitado;

    @Column(name = "CodigoQualificadorVisitado")
    private Integer codigoQualificadorVisitado;

    @Column(name = "NomeQualificadorVisitado", length = 30)
    private String nomeQualificadorVisitado;

    @Column(name = "TelefoneVisitado", length = 50)
    private String telefoneVisitado;

    // Construtores
    public HistoricoVisitado() {}

    public HistoricoVisitado(String documentoVisitado, Long codigoHistoricos,
                             String nomeSetorVisitado, String nomeUnidadeVisitado,
                             String nomeVisitado, String sobrenomeVisitado) {
        this.documentoVisitado = documentoVisitado;
        this.codigoHistoricos = codigoHistoricos;
        this.nomeSetorVisitado = nomeSetorVisitado;
        this.nomeUnidadeVisitado = nomeUnidadeVisitado;
        this.nomeVisitado = nomeVisitado;
        this.sobrenomeVisitado = sobrenomeVisitado;
    }

    // Getters e Setters
    public String getDocumentoVisitado() { return documentoVisitado; }
    public void setDocumentoVisitado(String documentoVisitado) { this.documentoVisitado = documentoVisitado; }

    public Long getCodigoHistoricos() { return codigoHistoricos; }
    public void setCodigoHistoricos(Long codigoHistoricos) { this.codigoHistoricos = codigoHistoricos; }

    public Integer getCodigoParticaoVisitado() { return codigoParticaoVisitado; }
    public void setCodigoParticaoVisitado(Integer codigoParticaoVisitado) { this.codigoParticaoVisitado = codigoParticaoVisitado; }

    public String getNomeParticaoVisitado() { return nomeParticaoVisitado; }
    public void setNomeParticaoVisitado(String nomeParticaoVisitado) { this.nomeParticaoVisitado = nomeParticaoVisitado; }

    public Integer getCodigoUnidadeVisitado() { return codigoUnidadeVisitado; }
    public void setCodigoUnidadeVisitado(Integer codigoUnidadeVisitado) { this.codigoUnidadeVisitado = codigoUnidadeVisitado; }

    public String getNomeUnidadeVisitado() { return nomeUnidadeVisitado; }
    public void setNomeUnidadeVisitado(String nomeUnidadeVisitado) { this.nomeUnidadeVisitado = nomeUnidadeVisitado; }

    public Integer getCodigoSetorVisitado() { return codigoSetorVisitado; }
    public void setCodigoSetorVisitado(Integer codigoSetorVisitado) { this.codigoSetorVisitado = codigoSetorVisitado; }

    public String getNomeSetorVisitado() { return nomeSetorVisitado; }
    public void setNomeSetorVisitado(String nomeSetorVisitado) { this.nomeSetorVisitado = nomeSetorVisitado; }

    public String getNomeVisitado() { return nomeVisitado; }
    public void setNomeVisitado(String nomeVisitado) { this.nomeVisitado = nomeVisitado; }

    public String getSobrenomeVisitado() { return sobrenomeVisitado; }
    public void setSobrenomeVisitado(String sobrenomeVisitado) { this.sobrenomeVisitado = sobrenomeVisitado; }

    public Boolean getVisitadoPrincipal() { return visitadoPrincipal; }
    public void setVisitadoPrincipal(Boolean visitadoPrincipal) { this.visitadoPrincipal = visitadoPrincipal; }

    public String getMensagemVisitado() { return mensagemVisitado; }
    public void setMensagemVisitado(String mensagemVisitado) { this.mensagemVisitado = mensagemVisitado; }

    public Integer getCodigoQualificadorVisitado() { return codigoQualificadorVisitado; }
    public void setCodigoQualificadorVisitado(Integer codigoQualificadorVisitado) { this.codigoQualificadorVisitado = codigoQualificadorVisitado; }

    public String getNomeQualificadorVisitado() { return nomeQualificadorVisitado; }
    public void setNomeQualificadorVisitado(String nomeQualificadorVisitado) { this.nomeQualificadorVisitado = nomeQualificadorVisitado; }

    public String getTelefoneVisitado() { return telefoneVisitado; }
    public void setTelefoneVisitado(String telefoneVisitado) { this.telefoneVisitado = telefoneVisitado; }
}