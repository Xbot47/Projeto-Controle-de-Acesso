package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {

    private static final String DB_NAME = "EditPro"; 

    public static Connection conectar() {
        Connection conexao = null;
        
        String serverName = ConfigManager.getProperty("db.server"); 
        
        // Se a configuração estiver vazia, usaremos um valor que forçará o loop no Main
        if (serverName == null || serverName.isEmpty()) {
             serverName = "LOCALHOST_PLACEHOLDER\\SQLEXPRESS"; 
        } else {
             // CORREÇÃO CRÍTICA: Substitui a barra simples por barra dupla para o JDBC
             serverName = serverName.replace("\\", "\\\\"); 
        }
        
        // URL FINAL CORRIGIDA
        String url = "jdbc:sqlserver://" + serverName + ";databaseName=" + DB_NAME + ";integratedSecurity=true;trustServerCertificate=true;loginTimeout=10;";
        
        try {
            // Tenta a conexão com o servidor configurado
            conexao = DriverManager.getConnection(url);
            
        } catch (SQLException e) {
            // O erro é tratado na classe Main
        }
        
        return conexao;
    }
}