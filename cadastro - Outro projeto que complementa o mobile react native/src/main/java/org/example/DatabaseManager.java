package org.example;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class DatabaseManager {
    private static final String DB_NAME = "DadosPessoais";
    private static DatabaseManager instance;
    
    private DatabaseManager() {}
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public boolean testServerConnection(String serverName) {
        String url = buildConnectionUrl(serverName, "");
        try (Connection conn = DriverManager.getConnection(url)) {
            return conn.isValid(3);
        } catch (SQLException e) {
            System.out.println("Falha na conexão com " + serverName + ": " + e.getMessage());
            return false;
        }
    }
    
    public List<String> detectAllPossibleInstances() {
        List<String> instances = new ArrayList<>();
        String computerName = System.getenv("COMPUTERNAME");
        
        if (computerName == null || computerName.isEmpty()) {
            computerName = "localhost";
        }
        
        String[] patterns = {
            computerName,
            computerName + "\\SQLEXPRESS",
            computerName + "\\SQLEXPRESS01", 
            computerName + "\\SQLEXPRESS02",
            computerName + "\\SQLEXPRESS03",
            computerName + "\\MSSQLSERVER",
            "localhost",
            "localhost\\SQLEXPRESS", 
            "localhost\\SQLEXPRESS01",
            "localhost\\SQLEXPRESS02",
            "localhost\\SQLEXPRESS03",
            "localhost\\MSSQLSERVER",
            ".",
            ".\\SQLEXPRESS",
            ".\\SQLEXPRESS01", 
            ".\\SQLEXPRESS02",
            ".\\SQLEXPRESS03",
            ".\\MSSQLSERVER",
            "127.0.0.1",
            "127.0.0.1\\SQLEXPRESS",
            "127.0.0.1\\SQLEXPRESS01",
            "127.0.0.1\\SQLEXPRESS02", 
            "127.0.0.1\\SQLEXPRESS03",
            "127.0.0.1\\MSSQLSERVER",
            "SQLEXPRESS03",
            "SQLEXPRESS01",
            "SQLEXPRESS02",
            "MSSQLSERVER"
        };
        
        for (String instance : patterns) {
            if (testServerConnection(instance)) {
                instances.add(instance);
                System.out.println("✅ Instância conectável: " + instance);
            }
        }
        
        return instances;
    }
    
    public List<String> findAllSqlInstances() {
        List<String> instances = new ArrayList<>();
        String computerName = System.getenv("COMPUTERNAME");
        
        if (computerName == null || computerName.isEmpty()) {
            computerName = "localhost";
        }
        
        instances.addAll(findInstancesInRegistry());
        instances.addAll(findInstancesInServices());
        instances.addAll(testCommonInstances(computerName));
        
        Set<String> uniqueInstances = new LinkedHashSet<>(instances);
        return new ArrayList<>(uniqueInstances);
    }
    
    private List<String> findInstancesInRegistry() {
        List<String> instances = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("reg query \"HKLM\\SOFTWARE\\Microsoft\\Microsoft SQL Server\" /s /f \"InstanceName\"");
            Scanner scanner = new Scanner(process.getInputStream());
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("InstanceName") && line.contains("REG_SZ")) {
                    String instanceName = line.split("REG_SZ")[1].trim();
                    if (!instanceName.isEmpty() && !instances.contains(instanceName)) {
                        instances.add(instanceName);
                    }
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("⚠ Não foi possível acessar o registro: " + e.getMessage());
        }
        return instances;
    }
    
    private List<String> findInstancesInServices() {
        List<String> instances = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("sc query type= service state= all");
            Scanner scanner = new Scanner(process.getInputStream());
            
            Pattern sqlPattern = Pattern.compile("SQL Server \\(([A-Z0-9_]+)\\)");
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                java.util.regex.Matcher matcher = sqlPattern.matcher(line);
                if (matcher.find()) {
                    String instanceName = matcher.group(1);
                    if (!instances.contains(instanceName)) {
                        instances.add(instanceName);
                    }
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("⚠ Não foi possível listar serviços: " + e.getMessage());
        }
        return instances;
    }
    
    private List<String> testCommonInstances(String computerName) {
        List<String> instances = new ArrayList<>();
        
        String[] instancePatterns = {
            computerName, computerName + "\\SQLEXPRESS", computerName + "\\SQLEXPRESS01", 
            computerName + "\\SQLEXPRESS02", computerName + "\\SQLEXPRESS03", computerName + "\\SQLEXPRESS04",
            computerName + "\\MSSQLSERVER", computerName + "\\MSSQLSERVER01", "localhost",
            "localhost\\SQLEXPRESS", "localhost\\SQLEXPRESS01", "localhost\\SQLEXPRESS02", 
            "localhost\\SQLEXPRESS03", "localhost\\MSSQLSERVER", ".", ".\\SQLEXPRESS",
            ".\\SQLEXPRESS01", ".\\SQLEXPRESS02", ".\\SQLEXPRESS03", ".\\MSSQLSERVER",
            "127.0.0.1", "127.0.0.1\\SQLEXPRESS", "127.0.0.1\\SQLEXPRESS01",
            "127.0.0.1\\SQLEXPRESS02", "127.0.0.1\\SQLEXPRESS03"
        };
        
        for (String instance : instancePatterns) {
            if (testServerConnection(instance)) {
                instances.add(instance);
            }
        }
        
        return instances;
    }
    
    public Optional<List<String>> detectSqlServers() {
        List<String> connectableInstances = detectAllPossibleInstances();
        
        if (connectableInstances.isEmpty()) {
            System.out.println("❌ Nenhuma instância SQL Server conectável encontrada");
            return Optional.empty();
        }
        
        System.out.println("✅ Instâncias conectáveis: " + connectableInstances);
        return Optional.of(connectableInstances);
    }
    
    public boolean createDatabase(String serverName) {
        System.out.println("🗃️ Criando banco de dados em: " + serverName);
        
        String masterUrl = buildConnectionUrl(serverName, "");
        
        try (Connection conn = DriverManager.getConnection(masterUrl);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("IF NOT EXISTS(SELECT * FROM sys.databases WHERE name = '" + DB_NAME + "') " +
                        "CREATE DATABASE " + DB_NAME);
            System.out.println("✅ Banco de dados criado/verificado");
            
            String dbUrl = buildConnectionUrl(serverName, DB_NAME);
            try (Connection dbConn = DriverManager.getConnection(dbUrl);
                 Statement dbStmt = dbConn.createStatement()) {
                
                dbStmt.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Pessoa' AND xtype='U') " +
                              "CREATE TABLE Pessoa (" +
                              "Id INT IDENTITY(1,1) PRIMARY KEY, " +
                              "Nome NVARCHAR(100) NOT NULL, " +
                              "Idade INT NOT NULL, " +
                              "DataCriacao DATETIME2 DEFAULT GETDATE())");
                System.out.println("✅ Tabela Pessoa criada/verificada");
            }
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao criar banco de dados: " + e.getMessage());
            return false;
        }
    }
    
    public boolean databaseExists(String serverName) {
    // Apenas verifica se existe, NÃO cria
        String url = buildConnectionUrl(serverName, DB_NAME);
            try (Connection conn = DriverManager.getConnection(url)) {
            return true; // Se conectou, o banco existe
        } catch (SQLException e) {
            return false; // Se falhou, o banco não existe
        }
    }
    
    private String buildConnectionUrl(String serverName, String databaseName) {
        StringBuilder url = new StringBuilder("jdbc:sqlserver://")
            .append(serverName.replace("\\", "\\\\"));
        
        if (!databaseName.isEmpty()) {
            url.append(";databaseName=").append(databaseName);
        }
        
        url.append(";integratedSecurity=true")
           .append(";trustServerCertificate=true")
           .append(";loginTimeout=5")
           .append(";encrypt=false");
        
        return url.toString();
    }
    
    public boolean isAnySqlServiceRunning() {
        try {
            Process process = Runtime.getRuntime().exec("sc query type= service state= all");
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("SQL Server") && line.contains("RUNNING")) {
                    scanner.close();
                    return true;
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.err.println("Erro ao verificar serviços: " + e.getMessage());
        }
        return false;
    }
}