package org.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DatabaseInitializer {

    private static final String DB_NAME = "EditPro";

    public void setupDatabase() {
        String serverName = ConfigManager.getProperty("db.server");

        if (serverName == null || serverName.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "Erro: Nome do servidor não configurado.",
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String urlMaster = "jdbc:sqlserver://" + serverName + ";integratedSecurity=true;trustServerCertificate=true;";

        try (Connection conn = DriverManager.getConnection(urlMaster)) {
            try (Statement stmt = conn.createStatement()) {
                String createDbSql = "IF NOT EXISTS(SELECT * FROM sys.databases WHERE name = '" + DB_NAME + "') " +
                                     "BEGIN " +
                                     "CREATE DATABASE " + DB_NAME + "; " +
                                     "END;";
                stmt.execute(createDbSql);
            }

            JOptionPane.showMessageDialog(null,
                "Banco de dados '" + DB_NAME + "' criado/verificado com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Erro ao criar banco de dados: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dbUrl = "jdbc:sqlserver://" + serverName + ";databaseName=" + DB_NAME + ";integratedSecurity=true;trustServerCertificate=true;";

        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (Statement stmt = conn.createStatement()) {
                // Criar as tabelas específicas do EditPro
                createEditProTables(stmt);
            }

            JOptionPane.showMessageDialog(null,
                "Tabelas do EditPro criadas/verificadas com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Erro ao criar tabelas: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createEditProTables(Statement stmt) throws SQLException {
        // Tabela Setores
        stmt.execute(
            "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Setores' AND xtype='U') " +
            "CREATE TABLE Setores (" +
            "Nome VARCHAR(30) NOT NULL)"
        );

        // Tabela Unidades
        stmt.execute(
            "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Unidades' AND xtype='U') " +
            "CREATE TABLE Unidades (" +
            "Nome VARCHAR(60) NOT NULL)"
        );

        // Tabela Visitados
        stmt.execute(
            "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Visitados' AND xtype='U') " +
            "CREATE TABLE Visitados (" +
            "Nome VARCHAR(60) NOT NULL, " +
            "SobreNome VARCHAR(60) NOT NULL, " +
            "DataHora SMALLDATETIME NOT NULL)"
        );

        // NOVO: Tabela CategoriasVisitantes
        stmt.execute(
            "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='CategoriasVisitantes' AND xtype='U') " +
            "CREATE TABLE CategoriasVisitantes (" +
            "Codigo INT IDENTITY(1,1) PRIMARY KEY NOT NULL, " +
            "Nome VARCHAR(30) NOT NULL, " +
            "CodigoParticao INT NOT NULL DEFAULT 1, " +
            "NomeParticao VARCHAR(30) NOT NULL DEFAULT 'Principal')"
        );
        
        // Tabela Visitantes (CORRIGIDA)
        stmt.execute(
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

        // Tabela Historicos (CORRIGIDO: EPIVisitante para VARCHAR(1))
        stmt.execute(
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
            "EPIVisitante VARCHAR(1) NOT NULL DEFAULT 'N', " + // CORREÇÃO AQUI
            "NomeVisitadoUsuarioEntrada VARCHAR(50) NULL, " +
            "NomeUsuarioEntrada VARCHAR(50) NULL)"
        );

        // Tabela HistoricosVisitados (Mantendo a DBO original DocumentoVisitado)
        stmt.execute(
            "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='HistoricosVisitados' AND xtype='U') " +
            "CREATE TABLE HistoricosVisitados (" +
            "DocumentoVisitado VARCHAR(30) NOT NULL, " + // MANTIDO DocumentoVisitado
            "Codigo_Historicos INT NOT NULL, " +
            "NomeUnidadeVisitado VARCHAR(30) NOT NULL, " +
            "NomeSetorVisitado VARCHAR(30) NOT NULL, " +
            "NomeVisitado VARCHAR(60) NOT NULL, " +
            "SobreNomeVisitado VARCHAR(30) NOT NULL, " +
            "CodigoParticaoVisitado INT NOT NULL DEFAULT 1, " +
            "CodigoUnidadeVisitado INT NOT NULL DEFAULT 1, " +
            "CodigoSetorVisitado INT NOT NULL DEFAULT 1, " +
            "PRIMARY KEY (DocumentoVisitado, Codigo_Historicos))"
        );
        
        // Inserir dados iniciais
        insertInitialData(stmt);
    }

    private void insertInitialData(Statement stmt) throws SQLException {
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
            
        } catch (SQLException e) {
            System.out.println("⚠️ Aviso: Não foi possível inserir dados iniciais: " + e.getMessage());
        }
    }

    public boolean restoreDatabase(String backupFilePath) {
        String serverName = ConfigManager.getProperty("db.server");

        if (serverName == null || serverName.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "Erro: Nome do servidor não configurado.",
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        File backupFile = new File(backupFilePath);
        if (!backupFile.exists()) {
            JOptionPane.showMessageDialog(null,
                "Arquivo de backup não encontrado:\n" + backupFilePath,
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // CORRIGIDO: Usar EditPro em vez de DadosPessoais
        String mdfPath = "C:\\Program Files\\Microsoft SQL Server\\MSSQL14.SQLEXPRESS03\\MSSQL\\DATA\\EditPro.mdf";
        String ldfPath = "C:\\Program Files\\Microsoft SQL Server\\MSSQL14.SQLEXPRESS03\\MSSQL\\DATA\\EditPro_log.ldf";

        String masterUrl = "jdbc:sqlserver://" + serverName + ";integratedSecurity=true;trustServerCertificate=true;";

        try (Connection conn = DriverManager.getConnection(masterUrl);
             Statement stmt = conn.createStatement()) {

            String logicalDataName = null;
            String logicalLogName = null;

            String fileListSql = "RESTORE FILELISTONLY FROM DISK = N'" + escapeSqlLiteral(backupFilePath) + "'";

            try (ResultSet rs = stmt.executeQuery(fileListSql)) {
                while (rs.next()) {
                    String logicalName = rs.getString("LogicalName");
                    String type = rs.getString("Type");
                    if (type != null && type.equalsIgnoreCase("L")) {
                        logicalLogName = logicalName;
                    } else {
                        if (logicalDataName == null) {
                            logicalDataName = logicalName;
                        }
                    }
                }
            } catch (SQLException ex) {
                logicalDataName = null;
                logicalLogName = null;
            }

            if (logicalDataName == null) {
                logicalDataName = DB_NAME; // Agora será EditPro
            }
            if (logicalLogName == null) {
                logicalLogName = DB_NAME + "_log"; // Agora será EditPro_log
            }

            String dropSql = "IF EXISTS(SELECT * FROM sys.databases WHERE name = '" + DB_NAME + "') " +
                             "BEGIN " +
                             "ALTER DATABASE " + DB_NAME + " SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
                             "DROP DATABASE " + DB_NAME + "; " +
                             "END";
            stmt.execute(dropSql);

            String restoreSql = "RESTORE DATABASE " + DB_NAME +
                                 " FROM DISK = N'" + escapeSqlLiteral(backupFilePath) + "'" +
                                 " WITH MOVE N'" + escapeSqlLiteral(logicalDataName) + "' TO N'" + escapeSqlLiteral(mdfPath) + "'," +
                                 " MOVE N'" + escapeSqlLiteral(logicalLogName) + "' TO N'" + escapeSqlLiteral(ldfPath) + "'," +
                                 " REPLACE";

            stmt.execute(restoreSql);

            JOptionPane.showMessageDialog(null,
                "Banco de dados '" + DB_NAME + "' restaurado com sucesso a partir de:\n" + backupFilePath,
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Erro ao restaurar backup: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean restoreDatabaseWithDialog() {
        File defaultDir = new File("C:\\Program Files\\Microsoft SQL Server\\MSSQL14.SQLEXPRESS03\\MSSQL\\Backup");
        JFileChooser fileChooser = new JFileChooser(defaultDir);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Backup SQL Server (*.bak)", "bak");
        fileChooser.setFileFilter(filter);

        fileChooser.setDialogTitle("Selecione o arquivo de backup (.bak)");
        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null,
                "Nenhum arquivo selecionado. Restauração cancelada.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        File backupFile = fileChooser.getSelectedFile();

        if (!backupFile.getName().toLowerCase().endsWith(".bak")) {
            JOptionPane.showMessageDialog(null,
                "O arquivo selecionado não é um backup válido (.bak).",
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return restoreDatabase(backupFile.getAbsolutePath());
    }

    private String escapeSqlLiteral(String s) {
        if (s == null) return null;
        return s.replace("'", "''");
    }
}