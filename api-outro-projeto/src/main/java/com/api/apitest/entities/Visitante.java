package com.api.apitest.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Visitantes")
public class Visitante {

    @Id
    @Column(name = "Documento", length = 30)
    private String documento;

    @Column(name = "Codigo_CategoriasVisitantes")
    private Integer codigoCategoriasVisitantes;

    @Column(name = "Codigo_Historicos")
    private Integer codigoHistoricos;

    @Column(name = "Codigo_HistoricosFotosVisitantes")
    private Integer codigoHistoricosFotosVisitantes;

    @Column(name = "Nome", length = 60, nullable = false)
    private String nome;

    @Column(name = "SobreNome", length = 30)
    private String sobrenome;

    @Column(name = "DiaAniv")
    private Integer diaAniv;

    @Column(name = "MesAniv")
    private Integer mesAniv;

    @Column(name = "DataHora")
    private LocalDateTime dataHora;

    @Column(name = "DataHoraIntegracao")
    private LocalDateTime dataHoraIntegracao;

    @Column(name = "Empresa", length = 60)
    private String empresa;

    @Column(name = "EPI")
    private Boolean epi = false;

    @Column(name = "VIP")
    private Boolean vip = false;

    @Column(name = "Deficiente")
    private Boolean deficiente = false;

    @Column(name = "Observacao", length = 8000)
    private String observacao;

    @Column(name = "UltimoHistoricoDataHoraPermanencia")
    private LocalDateTime ultimoHistoricoDataHoraPermanencia;

    @Column(name = "UltimoHistoricoDataHoraVisita")
    private LocalDateTime ultimoHistoricoDataHoraVisita;

    @Column(name = "HistoricoEmAbertoCodigoParticao", length = 50)
    private String historicoEmAbertoCodigoParticao;

    @Column(name = "HistoricoEmAbertoIdentificadorCartao", length = 20)
    private String historicoEmAbertoIdentificadorCartao;

    @Column(name = "HistoricoEmAbertoCartaoNaoPreCadastrado", length = 20)
    private String historicoEmAbertoCartaoNaoPreCadastrado;

    @Column(name = "NumeroVisitas")
    private Integer numeroVisitas = 0;

    // Campos extras (opcionais)
    @Column(name = "Extrast1", length = 60)
    private String extrast1;

    @Column(name = "Extrast2", length = 60)
    private String extrast2;

    @Column(name = "Extrast3", length = 60)
    private String extrast3;

    @Column(name = "Extrast4", length = 60)
    private String extrast4;

    @Column(name = "Extrast5", length = 60)
    private String extrast5;

    @Column(name = "Extrast6", length = 60)
    private String extrast6;

    @Column(name = "Extrast7", length = 60)
    private String extrast7;

    @Column(name = "Extrast8", length = 60)
    private String extrast8;

    @Column(name = "Extrast9", length = 60)
    private String extrast9;

    @Column(name = "Extrast10", length = 60)
    private String extrast10;

    @Column(name = "Extrast11", length = 60)
    private String extrast11;

    @Column(name = "Extrast12", length = 60)
    private String extrast12;

    @Column(name = "Extradt1")
    private LocalDateTime extradt1;

    @Column(name = "Extradt2")
    private LocalDateTime extradt2;

    @Column(name = "Extrabt1")
    private Boolean extrabt1;

    @Column(name = "Extrabt2")
    private Boolean extrabt2;

    @Column(name = "Extratx1", length = 8000)
    private String extratx1;

    @Column(name = "CATStatusVisitante", length = 1)
    private String catStatusVisitante;

    @Column(name = "Biometria1")
    @Lob
    private byte[] biometria1;

    @Column(name = "ZKBiometria1", columnDefinition = "TEXT")
    private String zkBiometria1;

    @Column(name = "CATBioTECF7StatusEnvioValidade", length = 1)
    private String catBioTECF7StatusEnvioValidade;

    @Column(name = "CATDataHoraUltimoLeitor")
    private LocalDateTime catDataHoraUltimoLeitor;

    @Column(name = "IntegracaoControle", length = 100)
    private String integracaoControle;

    // Campo virtual para categoria (não mapeado diretamente)
    @Transient
    private String categoria;

    // Construtores
    public Visitante() {
        this.dataHora = LocalDateTime.now();
        this.numeroVisitas = 0;
        this.epi = false;
        this.vip = false;
        this.deficiente = false;
    }

    public Visitante(String documento, String nome, String sobrenome, String categoria) {
        this();
        this.documento = documento.toUpperCase().replace(" ", "");
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.categoria = categoria;
    }

    // Getters e Setters
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) {
        this.documento = documento.toUpperCase().replace(" ", "");
    }

    public Integer getCodigoCategoriasVisitantes() { return codigoCategoriasVisitantes; }
    public void setCodigoCategoriasVisitantes(Integer codigoCategoriasVisitantes) {
        this.codigoCategoriasVisitantes = codigoCategoriasVisitantes;
    }

    public Integer getCodigoHistoricos() { return codigoHistoricos; }
    public void setCodigoHistoricos(Integer codigoHistoricos) { this.codigoHistoricos = codigoHistoricos; }

    public Integer getCodigoHistoricosFotosVisitantes() { return codigoHistoricosFotosVisitantes; }
    public void setCodigoHistoricosFotosVisitantes(Integer codigoHistoricosFotosVisitantes) {
        this.codigoHistoricosFotosVisitantes = codigoHistoricosFotosVisitantes;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSobrenome() { return sobrenome; }
    public void setSobrenome(String sobrenome) { this.sobrenome = sobrenome; }

    public Integer getDiaAniv() { return diaAniv; }
    public void setDiaAniv(Integer diaAniv) { this.diaAniv = diaAniv; }

    public Integer getMesAniv() { return mesAniv; }
    public void setMesAniv(Integer mesAniv) { this.mesAniv = mesAniv; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public LocalDateTime getDataHoraIntegracao() { return dataHoraIntegracao; }
    public void setDataHoraIntegracao(LocalDateTime dataHoraIntegracao) { this.dataHoraIntegracao = dataHoraIntegracao; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public Boolean getEpi() { return epi; }
    public void setEpi(Boolean epi) { this.epi = epi; }

    public Boolean getVip() { return vip; }
    public void setVip(Boolean vip) { this.vip = vip; }

    public Boolean getDeficiente() { return deficiente; }
    public void setDeficiente(Boolean deficiente) { this.deficiente = deficiente; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public LocalDateTime getUltimoHistoricoDataHoraPermanencia() { return ultimoHistoricoDataHoraPermanencia; }
    public void setUltimoHistoricoDataHoraPermanencia(LocalDateTime ultimoHistoricoDataHoraPermanencia) {
        this.ultimoHistoricoDataHoraPermanencia = ultimoHistoricoDataHoraPermanencia;
    }

    public LocalDateTime getUltimoHistoricoDataHoraVisita() { return ultimoHistoricoDataHoraVisita; }
    public void setUltimoHistoricoDataHoraVisita(LocalDateTime ultimoHistoricoDataHoraVisita) {
        this.ultimoHistoricoDataHoraVisita = ultimoHistoricoDataHoraVisita;
    }

    public String getHistoricoEmAbertoCodigoParticao() { return historicoEmAbertoCodigoParticao; }
    public void setHistoricoEmAbertoCodigoParticao(String historicoEmAbertoCodigoParticao) {
        this.historicoEmAbertoCodigoParticao = historicoEmAbertoCodigoParticao;
    }

    public String getHistoricoEmAbertoIdentificadorCartao() { return historicoEmAbertoIdentificadorCartao; }
    public void setHistoricoEmAbertoIdentificadorCartao(String historicoEmAbertoIdentificadorCartao) {
        this.historicoEmAbertoIdentificadorCartao = historicoEmAbertoIdentificadorCartao;
    }

    public String getHistoricoEmAbertoCartaoNaoPreCadastrado() { return historicoEmAbertoCartaoNaoPreCadastrado; }
    public void setHistoricoEmAbertoCartaoNaoPreCadastrado(String historicoEmAbertoCartaoNaoPreCadastrado) {
        this.historicoEmAbertoCartaoNaoPreCadastrado = historicoEmAbertoCartaoNaoPreCadastrado;
    }

    public Integer getNumeroVisitas() { return numeroVisitas; }
    public void setNumeroVisitas(Integer numeroVisitas) { this.numeroVisitas = numeroVisitas; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    // Método para incrementar visitas
    public void incrementarVisitas() {
        if (this.numeroVisitas == null) {
            this.numeroVisitas = 1;
        } else {
            this.numeroVisitas++;
        }
    }

    // Getters e Setters para campos extras (opcionais)
    public String getExtrast1() { return extrast1; }
    public void setExtrast1(String extrast1) { this.extrast1 = extrast1; }

    public String getExtrast2() { return extrast2; }
    public void setExtrast2(String extrast2) { this.extrast2 = extrast2; }

    public String getExtrast3() { return extrast3; }
    public void setExtrast3(String extrast3) { this.extrast3 = extrast3; }

    public String getExtrast4() { return extrast4; }
    public void setExtrast4(String extrast4) { this.extrast4 = extrast4; }

    public String getExtrast5() { return extrast5; }
    public void setExtrast5(String extrast5) { this.extrast5 = extrast5; }

    public String getExtrast6() { return extrast6; }
    public void setExtrast6(String extrast6) { this.extrast6 = extrast6; }

    public String getExtrast7() { return extrast7; }
    public void setExtrast7(String extrast7) { this.extrast7 = extrast7; }

    public String getExtrast8() { return extrast8; }
    public void setExtrast8(String extrast8) { this.extrast8 = extrast8; }

    public String getExtrast9() { return extrast9; }
    public void setExtrast9(String extrast9) { this.extrast9 = extrast9; }

    public String getExtrast10() { return extrast10; }
    public void setExtrast10(String extrast10) { this.extrast10 = extrast10; }

    public String getExtrast11() { return extrast11; }
    public void setExtrast11(String extrast11) { this.extrast11 = extrast11; }

    public String getExtrast12() { return extrast12; }
    public void setExtrast12(String extrast12) { this.extrast12 = extrast12; }

    public LocalDateTime getExtradt1() { return extradt1; }
    public void setExtradt1(LocalDateTime extradt1) { this.extradt1 = extradt1; }

    public LocalDateTime getExtradt2() { return extradt2; }
    public void setExtradt2(LocalDateTime extradt2) { this.extradt2 = extradt2; }

    public Boolean getExtrabt1() { return extrabt1; }
    public void setExtrabt1(Boolean extrabt1) { this.extrabt1 = extrabt1; }

    public Boolean getExtrabt2() { return extrabt2; }
    public void setExtrabt2(Boolean extrabt2) { this.extrabt2 = extrabt2; }

    public String getExtratx1() { return extratx1; }
    public void setExtratx1(String extratx1) { this.extratx1 = extratx1; }

    public String getCatStatusVisitante() { return catStatusVisitante; }
    public void setCatStatusVisitante(String catStatusVisitante) { this.catStatusVisitante = catStatusVisitante; }

    public byte[] getBiometria1() { return biometria1; }
    public void setBiometria1(byte[] biometria1) { this.biometria1 = biometria1; }

    public String getZkBiometria1() { return zkBiometria1; }
    public void setZkBiometria1(String zkBiometria1) { this.zkBiometria1 = zkBiometria1; }

    public String getCatBioTECF7StatusEnvioValidade() { return catBioTECF7StatusEnvioValidade; }
    public void setCatBioTECF7StatusEnvioValidade(String catBioTECF7StatusEnvioValidade) {
        this.catBioTECF7StatusEnvioValidade = catBioTECF7StatusEnvioValidade;
    }

    public LocalDateTime getCatDataHoraUltimoLeitor() { return catDataHoraUltimoLeitor; }
    public void setCatDataHoraUltimoLeitor(LocalDateTime catDataHoraUltimoLeitor) {
        this.catDataHoraUltimoLeitor = catDataHoraUltimoLeitor;
    }

    public String getIntegracaoControle() { return integracaoControle; }
    public void setIntegracaoControle(String integracaoControle) { this.integracaoControle = integracaoControle; }

    @Override
    public String toString() {
        return "Visitante{" +
                "documento='" + documento + '\'' +
                ", nome='" + nome + '\'' +
                ", sobrenome='" + sobrenome + '\'' +
                ", numeroVisitas=" + numeroVisitas +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}