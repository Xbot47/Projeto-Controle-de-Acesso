package com.api.apitest.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "CategoriasVisitantes")
public class CategoriasVisitantes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Codigo")
    private Long codigo;  // ⚠️ Mudei de Long para Integer para compatibilidade

    @Column(name = "Nome", length = 30, nullable = false)
    private String nome;

    // Construtores
    public CategoriasVisitantes() {}

    public CategoriasVisitantes(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "CategoriasVisitantes{" +
                "codigo=" + codigo +
                ", nome='" + nome + '\'' +
                '}';
    }
}