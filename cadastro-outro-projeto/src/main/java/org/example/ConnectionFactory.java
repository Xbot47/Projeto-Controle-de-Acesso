package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class ConnectionFactory {
    private static final String DB_NAME = "EditPro";
    private static final Logger logger = Logger.getLogger(ConnectionFactory.class.getName());
    
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("✅ Driver SQL Server carregado com sucesso");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERRO: Driver SQL Server JDBC não encontrado!");
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        String serverName = ConfigManager.getProperty("db.server");
        
        if (serverName == null || serverName.trim().isEmpty()) {
            throw new SQLException("Servidor de banco de dados não configurado");
        }
        
        String url = buildConnectionString(serverName, DB_NAME);
        logger.info("Tentando conexão com: " + url);
        
        return DriverManager.getConnection(url);
    }
    
    public static Connection getConnection(String serverName) throws SQLException {
        String url = buildConnectionString(serverName, DB_NAME);
        logger.info("Tentando conexão com: " + url);
        return DriverManager.getConnection(url);
    }
    
    private static String buildConnectionString(String serverName, String databaseName) {
        String cleanServerName = serverName.replace("\\\\", "\\");
        
        if (databaseName.isEmpty()) {
            return String.format(
                "jdbc:sqlserver://%s;integratedSecurity=true;trustServerCertificate=true;loginTimeout=30;encrypt=false",
                cleanServerName
            );
        } else {
            return String.format(
                "jdbc:sqlserver://%s;databaseName=%s;integratedSecurity=true;trustServerCertificate=true;loginTimeout=30;encrypt=false",
                cleanServerName, databaseName
            );
        }
    }
    
    public static boolean testConnection(String serverName) {
        try {
            System.out.println("🔌 Testando conexão com: " + serverName);
            
            // Testa APENAS a conexão com o servidor (SEM banco de dados)
            String testUrl = buildConnectionString(serverName, "");
            
            try (Connection conn = DriverManager.getConnection(testUrl)) {
                boolean serverConnected = conn.isValid(5);
                System.out.println("✅ Conexão com servidor OK: " + serverConnected);
                
                return serverConnected;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Falha na conexão: " + e.getMessage());
            return false;
        }
    }

    public static boolean testServerConnection(String serverName) {
        return testConnection(serverName);
    }
    
    // MÉTODO SEPARADO para criar banco (só é chamado quando o usuário escolher)
    public static boolean createDatabaseIfNotExists(String serverName) {
        try {
            String masterUrl = buildConnectionString(serverName, "");
            
            try (Connection conn = DriverManager.getConnection(masterUrl);
                 Statement stmt = conn.createStatement()) {
                
                // Cria o banco se não existir
                stmt.execute("IF NOT EXISTS(SELECT * FROM sys.databases WHERE name = 'EditPro') " +
                              "CREATE DATABASE EditPro");
                System.out.println("✅ Banco de dados criado/verificado");
                
                // Conecta ao banco e cria as tabelas EXATAS que você precisa
                String dbUrl = buildConnectionString(serverName, DB_NAME);
                try (Connection dbConn = DriverManager.getConnection(dbUrl);
                     Statement dbStmt = dbConn.createStatement()) {
                    
                    // Tabela Setores (Inalterada)
                    dbStmt.execute(
                        "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Setores' AND xtype='U') " +
                        "CREATE TABLE Setores (" +
                        "Nome VARCHAR(30) NOT NULL)"
                    );
                    System.out.println("✅ Tabela Setores criada/verificada");

                    // Tabela Unidades (Inalterada)
                    dbStmt.execute(
                        "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Unidades' AND xtype='U') " +
                        "CREATE TABLE Unidades (" +
                        "Nome VARCHAR(60) NOT NULL)"
                    );
                    System.out.println("✅ Tabela Unidades criada/verificada");

                    // Tabela Visitados (Inalterada)
                    dbStmt.execute(
                        "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Visitados' AND xtype='U') " +
                        "CREATE TABLE Visitados (" +
                        "Nome VARCHAR(60) NOT NULL, " +
                        "SobreNome VARCHAR(60) NOT NULL, " +
                        "DataHora SMALLDATETIME NOT NULL)"
                    );
                    System.out.println("✅ Tabela Visitados criada/verificada");
                    
                    // Tabela CategoriasVisitantes (Inalterada)
                    dbStmt.execute(
                        "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='CategoriasVisitantes' AND xtype='U') " +
                        "CREATE TABLE CategoriasVisitantes (" +
                        "Codigo INT IDENTITY(1,1) PRIMARY KEY NOT NULL, " +
                        "Nome VARCHAR(30) NOT NULL, " +
                        "CodigoParticao INT NOT NULL DEFAULT 1, " +
                        "NomeParticao VARCHAR(30) NOT NULL DEFAULT 'Principal')"
                    );
                    System.out.println("✅ Tabela CategoriasVisitantes criada/verificada");

                    // Tabela Visitantes (Inalterada da correção anterior)
                    dbStmt.execute(
                        "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Visitantes' AND xtype='U') " +
                        "CREATE TABLE Visitantes (" +
                        "Documento VARCHAR(30) PRIMARY KEY NOT NULL, " +
                        "Nome VARCHAR(60) NOT NULL, " +
                        "SobreNome VARCHAR(30) NULL, " +
                        "DataHora SMALLDATETIME NOT NULL, " +
                        "UltimoHistoricoDataHoraPermanencia SMALLDATETIME NULL, " +
                        "UltimoHistoricoDataHoraVisita SMALLDATETIME NULL, " +
                        "NumeroVisitas INT NULL," +
                        "Codigo_CategoriasVisitantes INT NOT NULL DEFAULT 1, " +
                        "Codigo_Historicos INT NULL," +
                        "CodigoParticao INT NOT NULL DEFAULT 1," +
                        "NomeParticao VARCHAR(30) NOT NULL DEFAULT 'Principal')"
                    );
                    System.out.println("✅ Tabela Visitantes criada/verificada (Estrutura Completa)");

                    // Tabela Historicos (CORRIGIDA: VIPVisitante adicionado com DEFAULT 0)
                    dbStmt.execute(
                        "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Historicos' AND xtype='U') " +
                        "CREATE TABLE Historicos (" +
                        "Codigo INT IDENTITY(1,1) PRIMARY KEY NOT NULL, " +
                        "DocumentoVisitante VARCHAR(30) NULL, " +
                        "NomeVisitante VARCHAR(60) NOT NULL, " +
                        "SobreNomeVisitante VARCHAR(30) NULL, " +
                        "NomeCategoriaVisitante VARCHAR(30) NOT NULL, " +
                        "DataHoraEntrada SMALLDATETIME NULL, " +
                        "CodigoParticao INT NOT NULL DEFAULT 1, " +
                        "NomeParticao VARCHAR(30) NOT NULL DEFAULT 'Principal', " +
                        "NomePortaria VARCHAR(30) NOT NULL DEFAULT 'Portaria Principal', " +
                        "NomeEstacao VARCHAR(30) NOT NULL DEFAULT 'Estação Central', " +
                        "CodigoCategoriaVisitante INT NOT NULL DEFAULT 1, " +
                        "EPIVisitante INT NOT NULL DEFAULT 0, " +
                        "VIPVisitante INT NOT NULL DEFAULT 0, " + // <<< CORREÇÃO CRÍTICA AQUI
                        "NomeVisitadoUsuarioEntrada VARCHAR(50) NULL, " +
                        "NomeUsuarioEntrada VARCHAR(50) NULL)"
                    );
                    System.out.println("✅ Tabela Historicos criada/verificada");
                    
                    // Tabela HistoricosVisitados (Inalterada da correção anterior)
                    dbStmt.execute(
                        "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='HistoricosVisitados' AND xtype='U') " +
                        "CREATE TABLE HistoricosVisitados (" +
                        "DocumentoVisitado VARCHAR(30) NOT NULL, " +
                        "Codigo_Historicos INT NOT NULL, " +
                        "NomeUnidadeVisitado VARCHAR(30) NOT NULL, " +
                        "NomeSetorVisitado VARCHAR(30) NOT NULL, " +
                        "NomeVisitado VARCHAR(60) NOT NULL, " +
                        "SobreNomeVisitado VARCHAR(30) NOT NULL, " +
                        "CodigoParticaoVisitado INT NOT NULL DEFAULT 1, " +
                        "CodigoUnidadeVisitado INT NOT NULL DEFAULT 1, " +
                        "CodigoSetorVisitado INT NOT NULL DEFAULT 1, " +
                        "NomeParticaoVisitado VARCHAR(30) NOT NULL DEFAULT 'Principal', " +
                        "PRIMARY KEY (DocumentoVisitado, Codigo_Historicos))"
                    );
                    System.out.println("✅ Tabela HistoricosVisitados criada/verificada");

                    // Inserir alguns dados iniciais de exemplo
                    insertInitialData(dbStmt);
                }
                
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao criar banco: " + e.getMessage());
            return false;
        }
    }

    // Método auxiliar para inserir dados iniciais
    private static void insertInitialData(Statement stmt) throws SQLException {
        try {
            // Inserir algumas categorias de exemplo (NOVO)
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM CategoriasVisitantes) " +
                "INSERT INTO CategoriasVisitantes (Nome) VALUES " +
                "('Morador'), ('Prestador'), ('Entrega'), ('Visitante')"
            );
            
            // Inserir alguns setores de exemplo
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM Setores) " +
                "INSERT INTO Setores (Nome) VALUES " +
                "('Bloco A'), ('Bloco B'), ('Bloco C'), ('Bloco D')"
            );
            
            // Inserir algumas unidades de exemplo
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM Unidades) " +
                "INSERT INTO Unidades (Nome) VALUES " +
                "('Rua das Flores'), ('Rua dos Pinheiros'), ('Avenida Principal'), ('Travessa da Paz')"
            );
            
            // Inserir alguns proprietários de exemplo
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM Visitados) " +
                "INSERT INTO Visitados (Nome, SobreNome, DataHora) VALUES " +
                "('João', 'Silva', GETDATE()), " +
                "('Maria', 'Santos', GETDATE()), " +
                "('Pedro', 'Oliveira', GETDATE())"
            );
            
            System.out.println("✅ Dados iniciais inseridos com sucesso");
            
        } catch (SQLException e) {
            System.out.println("⚠️ Aviso: Não foi possível inserir dados iniciais: " + e.getMessage());
        }
    }
}