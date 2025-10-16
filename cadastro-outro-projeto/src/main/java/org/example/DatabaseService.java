package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static DatabaseService instance;
    private final HistoricoDAO historicoDAO = new HistoricoDAO(); 

    private DatabaseService() {}

    public static synchronized DatabaseService getInstance() {
        if (instance == null) instance = new DatabaseService();
        return instance;
    }
    
    // =========================================================
    // Métodos Auxiliares de Banco (Metadados e Listas)
    // =========================================================
    
    public List<String> getColumnNames(String tableName) {
        List<String> columns = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    columns.add(
                        rs.getString("COLUMN_NAME") +
                        " (" + rs.getString("TYPE_NAME") + ") " +
                        "- NULL: " + rs.getString("IS_NULLABLE")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar colunas da tabela " + tableName + ": " + e.getMessage());
        }
        return columns;
    }

    public boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar tabela: " + e.getMessage());
        }
        return false;
    }

    private List<String> buscarLista(String tableName, String columnName) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName + " ORDER BY " + columnName;
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rs.getString(columnName));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar dados da tabela " + tableName + ": " + e.getMessage());
        }
        
        if (lista.isEmpty()) {
            lista.add(tableName.equals("CategoriasVisitantes") ? "Visitante" : "Não Aplicável");
        }
        return lista;
    }
    
    public List<String> buscarCategorias() {
        return buscarLista("CategoriasVisitantes", "Nome");
    }

    public List<String> buscarSetores() {
        return buscarLista("Setores", "Nome");
    }

    public List<String> buscarUnidades() {
        return buscarLista("Unidades", "Nome");
    }

    public List<String> buscarProprietarios() {
        List<String> proprietarios = new ArrayList<>();
        String sql = "SELECT Nome, SobreNome FROM Visitados ORDER BY Nome";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String nome = rs.getString("Nome") != null ? rs.getString("Nome") : "";
                String sobrenome = rs.getString("SobreNome") != null ? rs.getString("SobreNome") : "";
                proprietarios.add((nome + " " + sobrenome).trim());
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar proprietários: " + e.getMessage());
        }
        if (proprietarios.isEmpty()) {
            proprietarios.add("Proprietário Padrão");
        }
        return proprietarios;
    }
    
    // =========================================================
    // CORRIGIDO: Busca Endereços por Número/Setor (Usando DISTINCT)
    // =========================================================
    public List<String> buscarEnderecos(String busca) {
        List<String> enderecos = new ArrayList<>();
        
        String sql = """
            SELECT DISTINCT TOP 350 s.Nome AS Setor, u.Nome AS Unidade 
            FROM Setores s 
            CROSS JOIN Unidades u
            WHERE s.Nome LIKE ? OR u.Nome LIKE ? 
            ORDER BY 1, 2
        """;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + busca + "%");
            pstmt.setString(2, "%" + busca + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    enderecos.add(rs.getString("Setor") + "/" + rs.getString("Unidade"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar endereços: " + e.getMessage());
        }
        return enderecos;
    }
    
    // =================================================================
    // IMPLEMENTADO: Busca o Código do Setor pelo Nome (Resolve o erro Not Supported Yet)
    // =================================================================
    private int buscarCodigoSetorPorNome(String nomeSetor) {
        // Se a string de setor estiver no formato "Setor/Unidade", apenas pegamos a parte do setor
        if (nomeSetor.contains("/")) {
            nomeSetor = nomeSetor.split("/")[0].trim();
        }
        
        String sql = "SELECT Codigo FROM Setores WHERE Nome = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeSetor.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("Codigo");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar código do setor: " + e.getMessage());
        }
        return -1;
    }
    
    // NOVO MÉTODO: Busca o nome da categoria do último histórico (Para Autocompletar)
    public String buscarNomeCategoria(String placa) {
        String sql = "SELECT TOP 1 NomeCategoriaVisitante FROM Historicos WHERE DocumentoVisitante = ? ORDER BY DataHoraEntrada DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, placa.trim().toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NomeCategoriaVisitante");
                }
            }
        } catch (SQLException e) {
             System.err.println("Erro ao buscar categoria: " + e.getMessage());
        }
        return "Visitante"; // Padrão
    }
    
    // NOVO: Busca o último endereço registrado para a placa (Para Registro Rápido)
    public String buscarUltimoEndereco(String placa) {
        String sql = """
            SELECT TOP 1 HV.NomeSetorVisitado, HV.NomeUnidadeVisitado 
            FROM Historicos H
            INNER JOIN HistoricosVisitados HV ON H.Codigo = HV.Codigo_Historicos
            WHERE H.DocumentoVisitante = ? 
            ORDER BY H.DataHoraEntrada DESC
        """;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, placa.trim().toUpperCase());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String setor = rs.getString("NomeSetorVisitado");
                    String unidade = rs.getString("NomeUnidadeVisitado");
                    return setor + "/" + unidade; 
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar último endereço: " + e.getMessage());
        }
        return ""; // Retorna vazio se não encontrar
    }

    // 🔹 Buscar código da categoria
    private int buscarCodigoCategoria(String categoria) {
        String sql = "SELECT Codigo FROM CategoriasVisitantes WHERE Nome = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoria);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("Codigo");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar código da categoria: " + e.getMessage());
        }
        // Fallback para o primeiro código (Geralmente o 1)
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT TOP 1 Codigo FROM CategoriasVisitantes ORDER BY Codigo");
             ResultSet rs2 = stmt.executeQuery()) {
            if (rs2.next()) return rs2.getInt("Codigo");
        } catch (SQLException ex) { /* Ignorar */ }
        
        return 1; // Default
    }

    // 🔹 Criar categoria se não existir (se o usuário digitar nova)
    private void criarCategoriaSeNaoExistir(String categoriaNome) {
        String checkSql = "SELECT COUNT(*) FROM CategoriasVisitantes WHERE Nome = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, categoriaNome);
            try(ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return; // Já existe
            }
            
            String insertSql = "INSERT INTO CategoriasVisitantes (Nome) VALUES (?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, categoriaNome);
                insertStmt.executeUpdate();
                System.out.println("✅ Categoria criada: " + categoriaNome);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao criar categoria: " + e.getMessage());
        }
    }
    
    // 🔹 Buscar visitante (Busca a placa exata para o autocompletar na entrada)
    public Visitante buscarVisitantePorDocumento(String documento) {
        String sql = "SELECT Documento, Nome, SobreNome, NumeroVisitas, DataHora FROM Visitantes WHERE Documento = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, documento.trim().toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Visitante v = new Visitante();
                    v.setDocumento(rs.getString("Documento"));
                    v.setNome(rs.getString("Nome"));
                    v.setSobrenome(rs.getString("SobreNome"));
                    v.setNumeroVisitas(rs.getInt("NumeroVisitas"));
                    v.setDataHoraCadastro(rs.getTimestamp("DataHora"));
                    return v;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar visitante: " + e.getMessage());
        }
        return null;
    }

    // ====================================================================
    // CORRIGIDO: Busca Múltiplos Visitantes com LIKE (para exibir todos os matches)
    // ====================================================================
    public List<Visitante> buscarVisitantesPorDocumentoParcial(String termo) {
        List<Visitante> resultados = new ArrayList<>();
        String documentoLimpo = termo.trim().toUpperCase();
        
        // Estratégia: Busca agressiva em qualquer lugar do Documento
        String termoBusca = "%" + documentoLimpo + "%";

        String sql = "SELECT Documento, Nome, SobreNome, NumeroVisitas, DataHora FROM Visitantes WHERE Documento LIKE ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, termoBusca);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Visitante v = new Visitante();
                    v.setDocumento(rs.getString("Documento"));
                    v.setNome(rs.getString("Nome"));
                    v.setSobrenome(rs.getString("SobreNome"));
                    v.setNumeroVisitas(rs.getInt("NumeroVisitas"));
                    v.setDataHoraCadastro(rs.getTimestamp("DataHora"));
                    resultados.add(v);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar visitantes: " + e.getMessage());
        }
        return resultados;
    }

    // 🔹 Salvar visitante (Com todos os NOT NULL da dbo.Visitantes)
    public boolean salvarVisitante(String documento, String nome, String sobrenome, String categoria) {
        criarCategoriaSeNaoExistir(categoria);
        int codigoCategoria = buscarCodigoCategoria(categoria);

        // SQL CRÍTICO: Incluídas todas as colunas obrigatórias da dbo.Visitantes.
        String sql = """
            INSERT INTO Visitantes (
                Documento, Codigo_CategoriasVisitantes, Nome, DataHora,
                EPI, VIP, Deficiente, 
                CATStatusVisitante, CATBioTECF7StatusEnvioValidade, IntegracaoControle,
                SobreNome, NumeroVisitas
            ) VALUES (
                ?, ?, ?, GETDATE(),
                0, 0, 0,
                'N', 'N', 'N/A',
                ?, 1
            )
        """;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Parâmetros:
            pstmt.setString(1, documento.trim().toUpperCase());
            pstmt.setInt(2, codigoCategoria);
            pstmt.setString(3, nome);
            // SobreNome é 'Checked' (aceita NULL).
            pstmt.setString(4, sobrenome != null && !sobrenome.isEmpty() ? sobrenome : null); 
            
            pstmt.executeUpdate();

            System.out.println("✅ Novo Visitante salvo com sucesso.");
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar visitante: " + e.getMessage());
            return false;
        }
    }

    // 🔹 Atualizar contador de visitas
    public boolean atualizarVisitas(String documento) {
        String sql = "UPDATE Visitantes SET NumeroVisitas = ISNULL(NumeroVisitas, 0) + 1 WHERE Documento = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, documento.trim().toUpperCase());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar visitas: " + e.getMessage());
            return false;
        }
    }

    // Implementado: Registra a entrada na tabela Historicos (Retorna o ID PK)
    public int registrarEntradaHistorico(String placa, String nome, String sobrenome, String categoria) {
        return historicoDAO.registrarEntrada(placa, nome, sobrenome, categoria);
    }
    
    // =========================================================
    // AUXILIAR: Mapeia Setor/Unidade para o Proprietário Real
    // =========================================================
    private String buscarProprietarioReal(String setor, String unidade) {
        
        // 1. Encontra o Codigo_Setores (usando o nome do setor da visita)
        int codigoSetor = buscarCodigoSetorPorNome(setor);
        
        if (codigoSetor == -1) {
            System.err.println("Setor não encontrado: " + setor);
            return "Proprietário Não Mapeado";
        }
        
        // 2. Busca o primeiro Visitado na tabela Visitados com o Codigo_Setores encontrado.
        String sql = "SELECT TOP 1 Nome, SobreNome FROM Visitados WHERE Codigo_Setores = ? ORDER BY DataHora DESC";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, codigoSetor);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("Nome") != null ? rs.getString("Nome") : "";
                    String sobrenome = rs.getString("SobreNome") != null ? rs.getString("SobreNome") : "";
                    return (nome + " " + sobrenome).trim();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao buscar proprietário real: " + e.getMessage());
        }
        
        // Se não encontrar, retorna o valor padrão
        return "Proprietário Não Mapeado";
    }

    // Implementado: Registra o destino na tabela HistoricosVisitados
    public void registrarDestino(String placa, String setor, String unidade, String proprietarioCompleto, int codigoHistorico) {
        
        if (codigoHistorico < 1) {
            System.err.println("❌ Código de histórico inválido. Destino não registrado.");
            return;
        }

        // NOVO: Buscamos o nome do proprietário real
        String proprietarioReal = buscarProprietarioReal(setor, unidade); 
        
        // O valor do proprietarioReal será quebrado para Nome e Sobrenome para inserção
        String nomeVisitado, sobrenomeVisitado;
        String[] partes = proprietarioReal.split("\\s+", 2); // Divide em no máximo 2 partes (Nome e o resto)
        nomeVisitado = partes[0];
        sobrenomeVisitado = partes.length > 1 ? partes[1].trim() : ""; 

        String documentoUsadoNaPK = placa.trim().toUpperCase();

        // Assume valores padrão para os códigos
        int codParticao = 1;
        int codUnidade = 1;
        int codSetor = buscarCodigoSetorPorNome(setor); // Tenta buscar o código real
        if (codSetor == -1) codSetor = 1; 

        // SQL: Inclui a coluna NomeParticaoVisitado
        String sql = """
            INSERT INTO HistoricosVisitados (
                DocumentoVisitado, Codigo_Historicos, NomeUnidadeVisitado, NomeSetorVisitado,
                NomeVisitado, SobreNomeVisitado, CodigoParticaoVisitado, CodigoUnidadeVisitado, 
                CodigoSetorVisitado, NomeParticaoVisitado 
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, documentoUsadoNaPK); 
            pstmt.setInt(2, codigoHistorico);
            // Usamos os valores reais do setor e unidade (da GUI)
            pstmt.setString(3, unidade);
            pstmt.setString(4, setor); 
            // INSERIMOS O PROPRIETÁRIO REAL BUSCADO
            pstmt.setString(5, nomeVisitado); 
            pstmt.setString(6, sobrenomeVisitado);
            
            pstmt.setInt(7, codParticao);
            pstmt.setInt(8, codUnidade);
            pstmt.setInt(9, codSetor);
            pstmt.setString(10, "Principal"); 
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Destino registrado (HistoricosVisitados).");
            }
        } catch (SQLException e) {
            System.err.println("🚨 ERRO CRÍTICO ao registrar destino: " + e.getMessage());
            throw new RuntimeException("Falha na PK de HistoricosVisitados. Veja o log detalhado.", e);
        }
    }
}