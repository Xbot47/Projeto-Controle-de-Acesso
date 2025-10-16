package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PessoaDAO {
    
    public void salvar(String nome, int idade) throws SQLException {
        String sql = "INSERT INTO Pessoa (Nome, Idade) VALUES (?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nome);
            pstmt.setInt(2, idade);
            pstmt.executeUpdate();
        }
    }
    
    public List<String> buscar(String termo) {
        List<String> resultados = new ArrayList<>();
        String sql = "SELECT Nome, Idade FROM Pessoa WHERE Nome LIKE ? OR Idade = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + termo + "%");
            
            try {
                pstmt.setInt(2, Integer.parseInt(termo));
            } catch (NumberFormatException e) {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultados.add(String.format("Nome: %s, Idade: %d", 
                        rs.getString("Nome"), rs.getInt("Idade")));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar dados: " + e.getMessage());
        }
        
        return resultados;
    }
}