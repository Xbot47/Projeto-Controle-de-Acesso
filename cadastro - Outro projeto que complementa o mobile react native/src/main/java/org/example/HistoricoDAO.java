package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAO {

    // 🔹 Buscar TODOS os históricos (REMOVIDA LIMITAÇÃO TOP 50)
    public List<String> buscarTodosHistoricos() {
        List<String> todosHistoricos = new ArrayList<>();
        // Removido TOP 50. Usando TOP 500 para evitar sobrecarga, mas capturando mais dados.
        String sql = "SELECT TOP 10000 DocumentoVisitante, NomeVisitante, SobreNomeVisitante, NomeCategoriaVisitante, NomeParticao, NomePortaria, NomeEstacao, DataHoraEntrada FROM Historicos ORDER BY DataHoraEntrada DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String historico = String.format(
                    "Placa: %s | %s %s | Categoria: %s | Partição: %s | Portaria: %s | Estação: %s | Entrada: %s",
                    rs.getString("DocumentoVisitante"),
                    rs.getString("NomeVisitante"),
                    rs.getString("SobreNomeVisitante") != null ? rs.getString("SobreNomeVisitante") : "",
                    rs.getString("NomeCategoriaVisitante"),
                    rs.getString("NomeParticao") != null ? rs.getString("NomeParticao") : "N/A",
                    rs.getString("NomePortaria") != null ? rs.getString("NomePortaria") : "N/A",
                    rs.getString("NomeEstacao") != null ? rs.getString("NomeEstacao") : "N/A",
                    rs.getTimestamp("DataHoraEntrada")
                );
                todosHistoricos.add(historico);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os históricos: " + e.getMessage());
        }
        return todosHistoricos;
    }

    // 🔹 Busca histórico por documento (Exibe Proprietário Real - REMOVIDA LIMITAÇÃO TOP 50)
    public List<String> buscarHistoricoCompletoPorDocumento(String documento) {
        List<String> historicos = new ArrayList<>();
        String sql = """
            SELECT
                H.DataHoraEntrada, 
                H.NomeCategoriaVisitante, 
                HV.NomeSetorVisitado, 
                HV.NomeUnidadeVisitado, 
                HV.NomeVisitado, 
                HV.SobreNomeVisitado
            FROM 
                Historicos H
            INNER JOIN 
                HistoricosVisitados HV 
            ON 
                H.Codigo = HV.Codigo_Historicos
            WHERE 
                H.DocumentoVisitante = ? 
            ORDER BY 
                H.DataHoraEntrada DESC
        """;
        // NOTA: A limitação TOP 50 foi removida, garantindo todos os registros.

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, documento.trim().toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String nomeVisitado = rs.getString("NomeVisitado") != null ? rs.getString("NomeVisitado") : "";
                    String sobrenomeVisitado = rs.getString("SobreNomeVisitado") != null ? rs.getString("SobreNomeVisitado") : "";
                    
                    String proprietarioCompleto = (nomeVisitado + " " + sobrenomeVisitado).trim();
                    
                    String historico = String.format(
                        "Entrada: %s | Endereço: %s/%s | Categoria: %s | Proprietário: %s",
                        rs.getTimestamp("DataHoraEntrada"),
                        rs.getString("NomeSetorVisitado"),
                        rs.getString("NomeUnidadeVisitado"),
                        rs.getString("NomeCategoriaVisitante"),
                        // Exibe o Proprietário Real
                        proprietarioCompleto.isEmpty() ? "N/A" : proprietarioCompleto
                    );
                    historicos.add(historico);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar histórico COMPLETO: " + e.getMessage());
        }
        return historicos;
    }

    // 🔹 Registrar entrada no histórico (Com todos os campos NOT NULL)
    public int registrarEntrada(String documento, String nome, String sobrenome, String categoria) {
        String documentoLimpo = documento.trim().toUpperCase().replace(" ", "");
        int codigoCategoria = buscarCodigoCategoria(categoria);
        int codigoGerado = -1;

        // SQL CRÍTICO: Lista TODAS as colunas Unchecked + EPI, VIP, Deficiente e SobreNomeVisitante.
        String sql = """
            INSERT INTO Historicos (
                CodigoParticao, NomeParticao, NomePortaria, NomeEstacao,
                DocumentoVisitante, NomeVisitante, CodigoCategoriaVisitante, NomeCategoriaVisitante, 
                DataHoraEntrada, DocumentoVisitadoUsuarioEntrada, NomeVisitadoUsuarioEntrada, 
                NomeUsuarioEntrada, Estacionamento,
                SobreNomeVisitante, EPIVisitante, VIPVisitante, DeficienteVisitante
            ) VALUES (
                1, 'Principal', 'Portaria Principal', 'Estação Central',
                ?, ?, ?, ?, 
                GETDATE(), 'NA', 'Sistema', 
                'Sistema', 0, 
                ?, 0, 0, 0
            )
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { 

            // Parâmetros:
            pstmt.setString(1, documentoLimpo);
            pstmt.setString(2, nome);
            pstmt.setInt(3, codigoCategoria);
            pstmt.setString(4, categoria);
            
            // SobreNomeVisitante (Campo Checked/opcional)
            pstmt.setString(5, sobrenome != null && !sobrenome.isEmpty() ? sobrenome : null);

            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                 try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        codigoGerado = rs.getInt(1); 
                    }
                }
                System.out.println("✅ Histórico registrado. ID: " + codigoGerado);
                return codigoGerado; 
            }
            
            return -1; 
        } catch (SQLException e) {
            System.err.println("🚨 ERRO ao registrar histórico: " + e.getMessage());
            return -1;
        }
    }

    // 🔹 Buscar último código (OBSOLETO, mas mantido por segurança)
    public int buscarUltimoCodigo() {
        String sql = "SELECT MAX(Codigo) AS UltimoCodigo FROM Historicos";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt("UltimoCodigo");
        } catch (SQLException e) {
            System.err.println("Erro ao buscar último código: " + e.getMessage());
        }
        return -1;
    }

    // 🔹 Busca código da categoria (Inalterado)
    private int buscarCodigoCategoria(String categoriaNome) {
        String sql = "SELECT Codigo FROM CategoriasVisitantes WHERE Nome = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoriaNome);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("Codigo");
            }

            // Fallback: Tenta retornar a primeira categoria
            try (PreparedStatement stmt = conn.prepareStatement("SELECT TOP 1 Codigo FROM CategoriasVisitantes ORDER BY Codigo");
                 ResultSet rs2 = stmt.executeQuery()) {
                if (rs2.next()) return rs2.getInt("Codigo");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar código da categoria: " + e.getMessage());
        }
        return 1; // fallback
    }
}