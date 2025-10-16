package org.example;

import java.sql.Timestamp;

public class Visitante {
    private String documento;
    private String nome;
    private String sobrenome;
    private Integer numeroVisitas;
    private Timestamp dataHoraCadastro;
    
    // Getters e Setters
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getSobrenome() { return sobrenome; }
    public void setSobrenome(String sobrenome) { this.sobrenome = sobrenome; }
    
    public Integer getNumeroVisitas() { return numeroVisitas; }
    public void setNumeroVisitas(Integer numeroVisitas) { this.numeroVisitas = numeroVisitas; }
    
    public Timestamp getDataHoraCadastro() { return dataHoraCadastro; }
    public void setDataHoraCadastro(Timestamp dataHoraCadastro) { this.dataHoraCadastro = dataHoraCadastro; }
    
    @Override
    public String toString() {
        return String.format("Placa: %s | %s %s | Visitas: %d | Cadastro: %s",
            documento, 
            nome, 
            sobrenome != null ? sobrenome : "",
            numeroVisitas != null ? numeroVisitas : 0,
            dataHoraCadastro != null ? dataHoraCadastro.toString() : "N/A"
        );
    }
}