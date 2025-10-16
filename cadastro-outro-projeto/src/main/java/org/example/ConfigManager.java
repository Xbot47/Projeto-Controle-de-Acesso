package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.PessoasManager";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.properties";
    private static final Properties props = new Properties();
    
    static {
        loadConfiguration();
    }
    
    private static void loadConfiguration() {
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                props.load(Files.newInputStream(configPath));
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
        }
    }
    
    public static String getProperty(String key) {
        return props.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
    
    public static void setProperty(String key, String value) {
        props.setProperty(key, value);
        saveConfiguration();
    }
    
    private static void saveConfiguration() {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            props.store(Files.newOutputStream(Paths.get(CONFIG_FILE)), 
                       "Configurações do Gerenciador de Pessoas");
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração: " + e.getMessage());
        }
    }
    
    public static String getServerName() {
        return getProperty("db.server");
    }
    
    public static void setServerName(String serverName) {
        setProperty("db.server", serverName);
    }
}