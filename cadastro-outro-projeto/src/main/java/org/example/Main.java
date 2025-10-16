package org.example;

import javax.swing.*;
import java.awt.Window;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    
    public static void main(String[] args) {
        setSystemLookAndFeel();
        
        SwingUtilities.invokeLater(() -> {
            try {
                // SEMPRE mostrar a escolha inicial, independente da configuração
                showInitialChoiceDialog();
            } catch (Exception e) {
                handleFatalError("Erro fatal ao iniciar aplicação", e);
            }
        });
    }
    
    // MÉTODO ESTÁTICO PRINCIPAL - Chamado por todas as telas para voltar ao menu
    public static void showInitialChoiceDialog() {
        SwingUtilities.invokeLater(() -> { 
            Object[] options = {
                "🚀 Iniciar Sistema (Banco Existente)", 
                "🗃️ Criar Novo Banco do Zero", 
                "💾 Restaurar de Backup", 
                "🔧 Configurar Conexão", 
                "❌ Sair"
            };
            
            int choice = JOptionPane.showOptionDialog(null,
                "=== SISTEMA DE CONTROLE DE VISITANTES - EDITPRO ===\n\n" +
                "Escolha uma opção de inicialização:\n\n" +
                "• 🚀 Iniciar Sistema: Usa banco EDITPRO existente\n" +
                "• 🗃️ Criar Novo Banco: Cria banco e tabelas do zero\n" +
                "• 💾 Restaurar Backup: Restaura de arquivo .bak\n" +
                "• 🔧 Configurar Conexão: Configurar servidor SQL\n" +
                "• ❌ Sair: Fechar aplicação",
                "Escolha de Inicialização",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            handleInitialChoice(choice);
        });
    }
    
    private static void handleInitialChoice(int choice) {
        switch (choice) {
            case 0: // Iniciar Sistema (Banco Existente)
                if (isDatabaseConfiguredAndReady()) {
                    launchMainApplication();
                } else {
                    JOptionPane.showMessageDialog(null,
                        "❌ Banco de dados não está configurado ou acessível!\n\n" +
                        "Configure a conexão ou crie um novo banco primeiro.",
                        "Banco Não Disponível", JOptionPane.WARNING_MESSAGE);
                    showInitialChoiceDialog();
                }
                break;
                
            case 1: // Criar Novo Banco do Zero
                if (isServerConfigured()) {
                    showCreateDatabaseWizard();
                } else {
                    JOptionPane.showMessageDialog(null,
                        "🔧 Configure primeiro a conexão com o SQL Server.",
                        "Configuração Necessária", JOptionPane.INFORMATION_MESSAGE);
                    showSetupDialog();
                }
                break;
                
            case 2: // Restaurar de Backup
                if (isServerConfigured()) {
                    showRestoreBackupWizard();
                } else {
                    JOptionPane.showMessageDialog(null,
                        "🔧 Configure primeiro a conexão com o SQL Server.",
                        "Configuração Necessária", JOptionPane.INFORMATION_MESSAGE);
                    showSetupDialog();
                }
                break;
                
            case 3: // Configurar Conexão
                showSetupDialog();
                break;
                
            case 4: // Sair
            case -1: // Usuário fechou a janela (X)
                System.exit(0);
                break;
                
            default:
                System.exit(0);
        }
    }
    
    private static boolean isServerConfigured() {
        String serverName = ConfigManager.getServerName();
        return serverName != null && !serverName.trim().isEmpty() && 
               ConnectionFactory.testConnection(serverName);
    }
    
    private static boolean isDatabaseConfiguredAndReady() {
        String serverName = ConfigManager.getServerName();
        System.out.println("🔍 Verificando banco - Servidor: " + serverName);
        
        if (serverName == null || serverName.trim().isEmpty()) {
            System.out.println("❌ Servidor não configurado");
            return false;
        }
        
        // Testa conexão com o servidor
        boolean serverConnected = ConnectionFactory.testConnection(serverName);
        if (!serverConnected) {
            System.out.println("❌ Servidor não conectável");
            return false;
        }
        
        System.out.println("✅ Servidor conectado, verificando banco EditPro...");
        
        // Testa SPECIFICAMENTE o banco EditPro
        boolean databaseExists = testEditProDatabaseExistence(serverName);
        System.out.println("📊 Banco EditPro existe: " + databaseExists);
        
        return databaseExists;
    }

    // NOVO MÉTODO: Teste específico para o banco EditPro
    private static boolean testEditProDatabaseExistence(String serverName) {
        String url = "jdbc:sqlserver://" + serverName.replace("\\", "\\\\") + 
                    ";databaseName=EditPro;integratedSecurity=true;trustServerCertificate=true;loginTimeout=5";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            // Testa se as tabelas principais existem
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) as count FROM sys.tables WHERE name IN ('Visitantes', 'Historicos')")) {
                 
                if (rs.next() && rs.getInt("count") >= 2) {
                    System.out.println("✅ Banco EditPro configurado e com tabelas");
                    return true;
                }
            }
            System.out.println("⚠️ Banco existe mas faltam tabelas");
            return false;
            
        } catch (SQLException e) {
            System.out.println("❌ Banco EditPro não acessível: " + e.getMessage());
            return false;
        }
    }
    
    private static void showCreateDatabaseWizard() {
        String serverName = ConfigManager.getServerName();
        
        int confirm = JOptionPane.showConfirmDialog(null,
            "🗃️ CRIAR NOVO BANCO EDITPRO DO ZERO\n\n" +
            "📋 SERVIDOR: " + serverName + "\n" +
            "📊 AÇÃO: Criar banco EditPro com TODAS as tabelas\n" +
            "✅ TABELAS INCLUÍDAS:\n" +
            "   • Visitantes, Historicos, HistoricosVisitados\n" +
            "   • Setores, Unidades, Visitados\n\n" +
            "⚠️  ATENÇÃO: Esta ação é irreversível!\n" +
            "Deseja continuar?",
            "Confirmar Criação do Banco", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                try {
                    boolean success = ConnectionFactory.createDatabaseIfNotExists(serverName);
                    
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(null,
                                "🎉 BANCO EDITPRO CRIADO COM SUCESSO!\n\n" +
                                "Todas as tabelas foram criadas:\n" +
                                "• Visitantes, Historicos, HistoricosVisitados\n" +
                                "• Setores, Unidades, Visitados\n\n" +
                                "O sistema está pronto para controle de visitantes!",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                            
                            int abrirApp = JOptionPane.showConfirmDialog(null,
                                "Deseja abrir o sistema de controle de visitantes agora?",
                                "Abrir Sistema", JOptionPane.YES_NO_OPTION);
                            
                            if (abrirApp == JOptionPane.YES_OPTION) {
                                launchMainApplication();
                            } else {
                                showInitialChoiceDialog();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null,
                                "❌ FALHA NA CRIAÇÃO DO BANCO\n\n" +
                                "Possíveis causas:\n" +
                                "• Permissões insuficientes no SQL Server\n" +
                                "• Servidor não está respondendo\n" +
                                "• Banco já existe com conflitos",
                                "Erro na Criação", JOptionPane.ERROR_MESSAGE);
                            showInitialChoiceDialog();
                        }
                    });
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                            "❌ ERRO CRÍTICO: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                        showInitialChoiceDialog();
                    });
                }
            }).start();
        } else {
            showInitialChoiceDialog();
        }
    }
    
    private static void showRestoreBackupWizard() {
        String serverName = ConfigManager.getServerName();
        
        // Diretório inicial inteligente
        File initialDirectory = null;
        String[] possiblePaths = {
            "C:\\Program Files\\Microsoft SQL Server\\MSSQL15.SQLEXPRESS\\MSSQL\\Backup",
            "C:\\Program Files\\Microsoft SQL Server\\MSSQL14.SQLEXPRESS\\MSSQL\\Backup", 
            "C:\\Program Files\\Microsoft SQL Server\\MSSQL13.SQLEXPRESS\\MSSQL\\Backup",
            "C:\\Program Files\\Microsoft SQL Server\\MSSQL12.SQLEXPRESS\\MSSQL\\Backup",
            System.getProperty("user.home") + "\\Documents",
            System.getProperty("user.home") + "\\Desktop"
        };
        
        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                initialDirectory = dir;
                break;
            }
        }
        
        if (initialDirectory == null) {
            initialDirectory = new File(System.getProperty("user.home"));
        }
        
        JFileChooser fileChooser = new JFileChooser(initialDirectory);
        fileChooser.setDialogTitle("Selecionar Arquivo de Backup do EditPro (.bak)");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Backup SQL Server (*.bak)", "bak"));
        fileChooser.setApproveButtonText("Selecionar Backup");
        
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();
            
            if (!backupFile.getName().toLowerCase().endsWith(".bak")) {
                JOptionPane.showMessageDialog(null,
                    "❌ O arquivo selecionado não é um backup válido!\n\n" +
                    "Por favor, selecione um arquivo com extensão .bak",
                    "Arquivo Inválido", JOptionPane.ERROR_MESSAGE);
                showRestoreBackupWizard();
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(null,
                "⚠️ CONFIRMAR RESTAURAÇÃO DO BACKUP ⚠️\n\n" +
                "📁 ARQUIVO: " + backupFile.getName() + "\n" +
                "📊 SERVIDOR: " + serverName + "\n" +
                "💾 BANCO: EditPro\n\n" +
                "🚨 ATENÇÃO: Esta ação substituirá completamente\n" +
                "o banco EditPro atual se existir!\n\n" +
                "Deseja continuar?",
                "Confirmar Restauração", 
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                new Thread(() -> {
                    try {
                        System.out.println("🔄 Iniciando processo de restore...");
                        
                        DatabaseInitializer initializer = new DatabaseInitializer();
                        boolean success = initializer.restoreDatabase(backupFile.getAbsolutePath());
                        
                        SwingUtilities.invokeLater(() -> {
                            if (success) {
                                System.out.println("✅ Restore concluído com sucesso!");
                                
                                // CORREÇÃO CRÍTICA: Forçar verificação do banco
                                System.out.println("🔍 Verificando configuração pós-restore...");
                                boolean databaseReady = isDatabaseConfiguredAndReady();
                                
                                if (databaseReady) {
                                    JOptionPane.showMessageDialog(null,
                                        "🎉 SISTEMA PRONTO!\n\n" +
                                        "Backup restaurado com sucesso!\n" +
                                        "Banco EditPro configurado e acessível.\n\n" +
                                        "Todos os dados estão disponíveis.",
                                        "Sucesso", 
                                        JOptionPane.INFORMATION_MESSAGE);
                                    
                                    int abrirApp = JOptionPane.showConfirmDialog(null,
                                        "Deseja abrir o sistema de controle de visitantes agora?",
                                        "Abrir Sistema", JOptionPane.YES_NO_OPTION);
                                    
                                    if (abrirApp == JOptionPane.YES_OPTION) {
                                        launchMainApplication();
                                    } else {
                                        showInitialChoiceDialog();
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                        "⚠️ AVISO\n\n" +
                                        "Backup restaurado mas o sistema não detectou o banco.\n" +
                                        "Tente reiniciar o aplicativo.",
                                        "Aviso", 
                                        JOptionPane.WARNING_MESSAGE);
                                    showInitialChoiceDialog();
                                }
                            } else {
                                JOptionPane.showMessageDialog(null,
                                    "❌ FALHA NA RESTAURAÇÃO\n\n" +
                                    "Possíveis causas:\n" +
                                    "• Arquivo de backup corrompido\n" +
                                    "• Permissões insuficientes\n" +
                                    "• Versão incompatível do SQL Server\n" +
                                    "• Banco em uso por outro processo",
                                    "Erro na Restauração", 
                                    JOptionPane.ERROR_MESSAGE);
                                showInitialChoiceDialog();
                            }
                        });
                        
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null,
                                "❌ ERRO NA RESTAURAÇÃO: " + ex.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                            showInitialChoiceDialog();
                        });
                    }
                }).start();
            } else {
                showInitialChoiceDialog();
            }
        } else {
            showInitialChoiceDialog();
        }
    }
    
    private static void showSetupDialog() {
        SetupDialog dialog = new SetupDialog();
        if (dialog.showDialog()) {
            showInitialChoiceDialog(); // Volta para o menu inicial após configurar
        } else {
            showInitialChoiceDialog(); // Volta para o menu inicial se cancelar
        }
    }
    
    public static void launchMainApplication() {
        try {
            // Limpeza de janelas
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window instanceof JDialog) {
                    window.dispose();
                }
            }

            ControleVisitantesGUI app = new ControleVisitantesGUI();
            app.setVisible(true);
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
            
        } catch (Exception e) {
            handleFatalError("Erro ao iniciar interface principal", e);
        }
    }
    
    private static void setSystemLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível configurar a aparência do sistema: " + e.getMessage());
        }
    }
    
    private static void handleFatalError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
        
        JOptionPane.showMessageDialog(null,
            message + ":\n" + e.getMessage() + "\n\nO aplicativo será fechado.",
            "Erro Fatal", JOptionPane.ERROR_MESSAGE);
            
        System.exit(1);
    }
}