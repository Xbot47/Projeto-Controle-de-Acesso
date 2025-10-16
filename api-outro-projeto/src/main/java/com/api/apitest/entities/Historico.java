package com.api.apitest.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Historicos")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Codigo")
    private Long codigo;

    @Column(name = "DocumentoVisitante", length = 30)
    private String documentoVisitante;

    @Column(name = "NomeVisitante", length = 60, nullable = false)
    private String nomeVisitante;

    @Column(name = "SobreNomeVisitante", length = 30)
    private String sobrenomeVisitante;

    @Column(name = "NomeCategoriaVisitante", length = 30, nullable = false)
    private String nomeCategoriaVisitante;

    @Column(name = "DataHoraEntrada")
    private LocalDateTime dataHoraEntrada;

    @Column(name = "CodigoParticao", nullable = false)
    private Integer codigoParticao = 1;

    @Column(name = "NomeParticao", length = 30, nullable = false)
    private String nomeParticao = "Principal";

    @Column(name = "NomePortaria", length = 30, nullable = false)
    private String nomePortaria = "Portaria Principal";

    @Column(name = "NomeEstacao", length = 30, nullable = false)
    private String nomeEstacao = "Estação Central";

    @Column(name = "CodigoCategoriaVisitante", nullable = false)
    private Integer codigoCategoriaVisitante = 1;

    // ✅ CORREÇÃO: Campos booleanos como Boolean (não String/Integer)
    @Column(name = "EPIVisitante", nullable = false)
    private Boolean epiVisitante = false;

    @Column(name = "VIPVisitante", nullable = false)
    private Boolean vipVisitante = false;

    @Column(name = "DeficienteVisitante", nullable = false)
    private Boolean deficienteVisitante = false;

    // ✅ CORREÇÃO: Campo obrigatório adicionado
    @Column(name = "DocumentoVisitadoUsuarioEntrada", length = 30, nullable = false)
    private String documentoVisitadoUsuarioEntrada = "00000000000";

    @Column(name = "NomeVisitadoUsuarioEntrada", length = 50)
    private String nomeVisitadoUsuarioEntrada;

    @Column(name = "NomeUsuarioEntrada", length = 50)
    private String nomeUsuarioEntrada;

    // Construtores
    public Historico() {
        this.dataHoraEntrada = LocalDateTime.now();
    }

    public Historico(String documentoVisitante, String nomeVisitante, String sobrenomeVisitante, String nomeCategoriaVisitante) {
        this();
        this.documentoVisitante = documentoVisitante.toUpperCase().replace(" ", "");
        this.nomeVisitante = nomeVisitante;
        this.sobrenomeVisitante = sobrenomeVisitante;
        this.nomeCategoriaVisitante = nomeCategoriaVisitante;
        this.nomeUsuarioEntrada = "Sistema Mobile";
        this.nomeVisitadoUsuarioEntrada = "Sistema";
        this.documentoVisitadoUsuarioEntrada = "00000000000"; // ✅ Valor padrão
    }

    // Getters e Setters
    public Long getCodigo() { return codigo; }
    public void setCodigo(Long codigo) { this.codigo = codigo; }

    public String getDocumentoVisitante() { return documentoVisitante; }
    public void setDocumentoVisitante(String documentoVisitante) {
        this.documentoVisitante = documentoVisitante.toUpperCase().replace(" ", "");
    }

    public String getNomeVisitante() { return nomeVisitante; }
    public void setNomeVisitante(String nomeVisitante) { this.nomeVisitante = nomeVisitante; }

    public String getSobrenomeVisitante() { return sobrenomeVisitante; }
    public void setSobrenomeVisitante(String sobrenomeVisitante) { this.sobrenomeVisitante = sobrenomeVisitante; }

    public String getNomeCategoriaVisitante() { return nomeCategoriaVisitante; }
    public void setNomeCategoriaVisitante(String nomeCategoriaVisitante) { this.nomeCategoriaVisitante = nomeCategoriaVisitante; }

    public LocalDateTime getDataHoraEntrada() { return dataHoraEntrada; }
    public void setDataHoraEntrada(LocalDateTime dataHoraEntrada) { this.dataHoraEntrada = dataHoraEntrada; }

    public Integer getCodigoParticao() { return codigoParticao; }
    public void setCodigoParticao(Integer codigoParticao) { this.codigoParticao = codigoParticao; }

    public String getNomeParticao() { return nomeParticao; }
    public void setNomeParticao(String nomeParticao) { this.nomeParticao = nomeParticao; }

    public String getNomePortaria() { return nomePortaria; }
    public void setNomePortaria(String nomePortaria) { this.nomePortaria = nomePortaria; }

    public String getNomeEstacao() { return nomeEstacao; }
    public void setNomeEstacao(String nomeEstacao) { this.nomeEstacao = nomeEstacao; }

    public Integer getCodigoCategoriaVisitante() { return codigoCategoriaVisitante; }
    public void setCodigoCategoriaVisitante(Integer codigoCategoriaVisitante) {
        this.codigoCategoriaVisitante = codigoCategoriaVisitante;
    }

    // ✅ CORREÇÃO: Getters/Setters booleanos
    public Boolean getEpiVisitante() { return epiVisitante; }
    public void setEpiVisitante(Boolean epiVisitante) { this.epiVisitante = epiVisitante; }

    public Boolean getDeficienteVisitante() { return deficienteVisitante; }
    public void setDeficienteVisitante(Boolean deficienteVisitante) { this.deficienteVisitante = deficienteVisitante; }

    // ✅ CORREÇÃO: Getter/Setter do novo campo
    public String getDocumentoVisitadoUsuarioEntrada() { return documentoVisitadoUsuarioEntrada; }
    public void setDocumentoVisitadoUsuarioEntrada(String documentoVisitadoUsuarioEntrada) {
        this.documentoVisitadoUsuarioEntrada = documentoVisitadoUsuarioEntrada;
    }

    public Boolean getVipVisitante() { return vipVisitante; }
    public void setVipVisitante(Boolean vipVisitante) { this.vipVisitante = vipVisitante; }

    public String getNomeVisitadoUsuarioEntrada() { return nomeVisitadoUsuarioEntrada; }
    public void setNomeVisitadoUsuarioEntrada(String nomeVisitadoUsuarioEntrada) {
        this.nomeVisitadoUsuarioEntrada = nomeVisitadoUsuarioEntrada;
    }

    public String getNomeUsuarioEntrada() { return nomeUsuarioEntrada; }
    public void setNomeUsuarioEntrada(String nomeUsuarioEntrada) {
        this.nomeUsuarioEntrada = nomeUsuarioEntrada;
    }

    @Override
    public String toString() {
        return "Historico{" +
                "codigo=" + codigo +
                ", documentoVisitante='" + documentoVisitante + '\'' +
                ", nomeVisitante='" + nomeVisitante + '\'' +
                ", codigoCategoriaVisitante=" + codigoCategoriaVisitante +
                '}';
    }
}