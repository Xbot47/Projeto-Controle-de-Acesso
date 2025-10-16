package com.api.apitest.dto;

public class RegistroEntradaDTO {
    private String documento;
    private String nome;
    private String sobrenome;
    private String categoria;
    private Integer codigoCategoria; // ✅ CAMPO ADICIONADO
    private String setor;
    private String unidade;
    private String proprietario;
    private String observacao; // ✅ CAMPO ADICIONADO

    // Construtores
    public RegistroEntradaDTO() {}

    public RegistroEntradaDTO(String documento, String nome, String categoria, String setor, String unidade) {
        this.documento = documento;
        this.nome = nome;
        this.categoria = categoria;
        this.setor = setor;
        this.unidade = unidade;
    }

    // ✅ CONSTRUTOR COMPLETO
    public RegistroEntradaDTO(String documento, String nome, String sobrenome, String categoria,
                              Integer codigoCategoria, String setor, String unidade,
                              String proprietario, String observacao) {
        this.documento = documento;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.categoria = categoria;
        this.codigoCategoria = codigoCategoria;
        this.setor = setor;
        this.unidade = unidade;
        this.proprietario = proprietario;
        this.observacao = observacao;
    }

    // Getters e Setters
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSobrenome() { return sobrenome; }
    public void setSobrenome(String sobrenome) { this.sobrenome = sobrenome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getCodigoCategoria() { return codigoCategoria; } // ✅ GETTER ADICIONADO
    public void setCodigoCategoria(Integer codigoCategoria) { this.codigoCategoria = codigoCategoria; } // ✅ SETTER ADICIONADO

    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public String getProprietario() { return proprietario; }
    public void setProprietario(String proprietario) { this.proprietario = proprietario; }

    public String getObservacao() { return observacao; } // ✅ GETTER ADICIONADO
    public void setObservacao(String observacao) { this.observacao = observacao; } // ✅ SETTER ADICIONADO

    public boolean isValid() {
        return documento != null && !documento.trim().isEmpty() &&
                nome != null && !nome.trim().isEmpty() &&
                setor != null && !setor.trim().isEmpty() &&
                unidade != null && !unidade.trim().isEmpty();
    }

    // ✅ MÉTODO toString PARA DEBUG
    @Override
    public String toString() {
        return "RegistroEntradaDTO{" +
                "documento='" + documento + '\'' +
                ", nome='" + nome + '\'' +
                ", sobrenome='" + sobrenome + '\'' +
                ", categoria='" + categoria + '\'' +
                ", codigoCategoria=" + codigoCategoria +
                ", setor='" + setor + '\'' +
                ", unidade='" + unidade + '\'' +
                ", proprietario='" + proprietario + '\'' +
                ", observacao='" + observacao + '\'' +
                '}';
    }
}