package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class InstallerGUI extends JFrame {
    private JTextArea logArea;
    
    public InstallerGUI() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Gerenciador do Banco EditPro - Criar/Restaurar");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel tituloLabel = new JLabel("🗃️ GERENCIADOR DO BANCO EDITPRO");
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 16));
        tituloLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Painel de informações
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        String serverInfo = ConfigManager.getServerName();
        if (serverInfo == null || serverInfo.isEmpty()) {
            serverInfo = "❌ Não configurado";
        }
        JLabel infoLabel = new JLabel("Servidor SQL: " + serverInfo);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel descLabel = new JLabel("Selecione uma operação para gerenciar o banco EditPro:");
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        infoPanel.add(descLabel, BorderLayout.CENTER);
        
        // Área de log
        logArea = new JTextArea(12, 50);
        logArea.setEditable(false);
        logArea.setBackground(new Color(240, 240, 240));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log de Operações"));
        
        // Painel de botões principais
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton criarDbButton = new JButton("🗃️ 1. CRIAR NOVO BANCO EDITPRO");
        criarDbButton.setBackground(new Color(34, 139, 34));
        criarDbButton.setForeground(Color.WHITE);
        criarDbButton.setFont(new Font("Arial", Font.BOLD, 12));
        criarDbButton.addActionListener(this::criarBancoDados);
        
        JButton restaurarButton = new JButton("💾 2. RESTAURAR DE BACKUP");
        restaurarButton.setBackground(new Color(70, 130, 180));
        restaurarButton.setForeground(Color.WHITE);
        restaurarButton.setFont(new Font("Arial", Font.BOLD, 12));
        restaurarButton.addActionListener(this::restaurarBackup);
        
        JButton verificarButton = new JButton("🔍 3. VERIFICAR BANCO ATUAL");
        verificarButton.setBackground(new Color(255, 165, 0));
        verificarButton.setForeground(Color.WHITE);
        verificarButton.setFont(new Font("Arial", Font.BOLD, 12));
        verificarButton.addActionListener(this::verificarBanco);
        
        buttonPanel.add(criarDbButton);
        buttonPanel.add(restaurarButton);
        buttonPanel.add(verificarButton);
        
        // Painel inferior com botão voltar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton voltarButton = new JButton("↩️ Voltar para Menu Principal");
        voltarButton.addActionListener(e -> {
            dispose();
            Main.showInitialChoiceDialog();
        });
        bottomPanel.add(voltarButton);
        
        // Montagem do layout
        mainPanel.add(tituloLabel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(mainPanel, BorderLayout.NORTH);
        contentPanel.add(logScroll, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(contentPanel);
        
        logMessage("=== GERENCIADOR DO BANCO EDITPRO ===");
        logMessage("Servidor: " + serverInfo);
        logMessage("Pronto para operações.");
        logMessage("Selecione uma opção acima.");
    }
    
    private void criarBancoDados(ActionEvent e) {
        String serverName = ConfigManager.getServerName();
        
        if (serverName == null || serverName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Servidor SQL não configurado!\n\n" +
                "Configure primeiro a conexão com o SQL Server\n" +
                "através da opção 'Configurar Conexão' no menu principal.",
                "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Deseja criar o banco de dados 'EditPro'?\n\n" +
            "📋 SERVIDOR: " + serverName + "\n" +
            "📊 AÇÃO: Criar banco EditPro com TODAS as tabelas\n" +
            "✅ TABELAS INCLUÍDAS:\n" +
            "   • Visitantes, Historicos, HistoricosVisitados\n" +
            "   • Setores, Unidades, Visitados\n\n" +
            "Esta ação é irreversível. Continuar?",
            "Confirmar Criação do Banco EditPro", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                try {
                    logMessage("\n" + "=".repeat(50));
                    logMessage("INICIANDO CRIAÇÃO DO BANCO EDITPRO");
                    logMessage("=".repeat(50));
                    logMessage("Servidor: " + serverName);
                    logMessage("📦 Preparando para criar banco e tabelas...");
                    
                    boolean success = ConnectionFactory.createDatabaseIfNotExists(serverName);
                    
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            logMessage("✅ BANCO EDITPRO CRIADO COM SUCESSO!");
                            logMessage("✅ Todas as tabelas foram criadas automaticamente");
                            logMessage("✅ Sistema pronto para uso");
                            
                            JOptionPane.showMessageDialog(this, 
                                "🎉 BANCO EDITPRO CRIADO COM SUCESSO!\n\n" +
                                "Todas as tabelas foram criadas:\n" +
                                "• Visitantes, Historicos, HistoricosVisitados\n" +
                                "• Setores, Unidades, Visitados\n\n" +
                                "O sistema está pronto para controle de visitantes!",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                            
                            int abrirApp = JOptionPane.showConfirmDialog(this, 
                                "Deseja abrir o sistema de controle de visitantes agora?",
                                "Abrir Sistema", JOptionPane.YES_NO_OPTION);
                            
                            if (abrirApp == JOptionPane.YES_OPTION) {
                                dispose();
                                Main.launchMainApplication();
                            }
                        } else {
                            logMessage("❌ FALHA NA CRIAÇÃO DO BANCO");
                            JOptionPane.showMessageDialog(this, 
                                "Falha ao criar banco de dados EditPro.\n\n" +
                                "Possíveis causas:\n" +
                                "• Permissões insuficientes no SQL Server\n" +
                                "• Servidor não está respondendo\n" +
                                "• Banco já existe com conflitos",
                                "Erro na Criação", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        logMessage("❌ ERRO CRÍTICO: " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, 
                            "Erro durante a criação do banco:\n" + ex.getMessage(), 
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }
    
    private void restaurarBackup(ActionEvent e) {
        String serverName = ConfigManager.getServerName();
        
        if (serverName == null || serverName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Servidor SQL não configurado!\n\nConfigure primeiro a conexão com o SQL Server.", 
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
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
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();
            
            if (!backupFile.getName().toLowerCase().endsWith(".bak")) {
                JOptionPane.showMessageDialog(this, 
                    "O arquivo selecionado não é um backup válido!\n\n" +
                    "Por favor, selecione um arquivo com extensão .bak",
                    "Arquivo Inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, 
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
                        logMessage("\n" + "=".repeat(50));
                        logMessage("INICIANDO RESTAURAÇÃO DO BACKUP");
                        logMessage("=".repeat(50));
                        logMessage("Arquivo: " + backupFile.getAbsolutePath());
                        logMessage("Servidor: " + serverName);
                        logMessage("📦 Iniciando processo de restauração...");
                        
                        DatabaseInitializer initializer = new DatabaseInitializer();
                        boolean success = initializer.restoreDatabase(backupFile.getAbsolutePath());
                        
                        SwingUtilities.invokeLater(() -> {
                            if (success) {
                                logMessage("✅ BACKUP RESTAURADO COM SUCESSO!");
                                logMessage("✅ Banco EditPro restaurado e pronto");
                                logMessage("✅ Todos os dados foram importados");
                                
                                JOptionPane.showMessageDialog(this, 
                                    "✅ BACKUP RESTAURADO COM SUCESSO!\n\n" +
                                    "O banco EditPro foi completamente restaurado\n" +
                                    "a partir do arquivo de backup.\n\n" +
                                    "Todos os dados estão disponíveis para uso.",
                                    "Restauração Bem-sucedida", 
                                    JOptionPane.INFORMATION_MESSAGE);
                                
                                int abrirApp = JOptionPane.showConfirmDialog(this, 
                                    "Deseja abrir o sistema de controle de visitantes agora?",
                                    "Abrir Sistema", JOptionPane.YES_NO_OPTION);
                                
                                if (abrirApp == JOptionPane.YES_OPTION) {
                                    dispose();
                                    Main.launchMainApplication();
                                }
                            } else {
                                logMessage("❌ FALHA NA RESTAURAÇÃO");
                                JOptionPane.showMessageDialog(this, 
                                    "Falha ao restaurar o backup.\n\n" +
                                    "Possíveis causas:\n" +
                                    "• Arquivo de backup corrompido\n" +
                                    "• Permissões insuficientes\n" +
                                    "• Versão incompatível do SQL Server\n" +
                                    "• Banco em uso por outro processo",
                                    "Erro na Restauração", 
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            logMessage("❌ ERRO NA RESTAURAÇÃO: " + ex.getMessage());
                            JOptionPane.showMessageDialog(this, 
                                "Erro durante a restauração:\n" + ex.getMessage(), 
                                "Erro", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            }
        }
    }
    
    private void verificarBanco(ActionEvent e) {
        String serverName = ConfigManager.getServerName();
        
        if (serverName == null || serverName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Servidor SQL não configurado!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        new Thread(() -> {
            try {
                logMessage("\n" + "=".repeat(50));
                logMessage("VERIFICANDO STATUS DO BANCO EDITPRO");
                logMessage("=".repeat(50));
                
                // Testa conexão com o servidor
                logMessage("🔌 Testando conexão com servidor...");
                boolean serverOk = ConnectionFactory.testConnection(serverName);
                
                if (!serverOk) {
                    logMessage("❌ Servidor não está acessível");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "Servidor SQL não está respondendo.", 
                            "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }
                
                logMessage("✅ Servidor conectado com sucesso");
                
                // Testa conexão com o banco EditPro
                logMessage("🗃️ Verificando banco EditPro...");
                boolean databaseExists = testEditProDatabase(serverName);
                
                SwingUtilities.invokeLater(() -> {
                    if (databaseExists) {
                        logMessage("✅ Banco EditPro encontrado e acessível");
                        logMessage("✅ Sistema está configurado corretamente");
                        
                        JOptionPane.showMessageDialog(this,
                            "✅ STATUS DO SISTEMA: OK\n\n" +
                            "📊 Servidor: " + serverName + "\n" +
                            "🗃️ Banco: EditPro (Acessível)\n" +
                            "🚀 Sistema: Pronto para uso\n\n" +
                            "O sistema está configurado corretamente\n" +
                            "e pronto para controle de visitantes.",
                            "Verificação Concluída",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        logMessage("❌ Banco EditPro não encontrado");
                        logMessage("💡 Use 'Criar Novo Banco' para configurar");
                        
                        JOptionPane.showMessageDialog(this,
                            "⚠️ BANCO NÃO ENCONTRADO\n\n" +
                            "O banco EditPro não existe ou não está acessível.\n\n" +
                            "Para usar o sistema, você precisa:\n" +
                            "1. Criar um novo banco EditPro, OU\n" +
                            "2. Restaurar de um backup existente",
                            "Banco Não Encontrado",
                            JOptionPane.WARNING_MESSAGE);
                    }
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    logMessage("❌ ERRO NA VERIFICAÇÃO: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Erro durante a verificação:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private boolean testEditProDatabase(String serverName) {
        String url = "jdbc:sqlserver://" + serverName.replace("\\", "\\\\") + 
                    ";databaseName=EditPro;integratedSecurity=true;trustServerCertificate=true;loginTimeout=5";
        
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url)) {
            return true;
        } catch (java.sql.SQLException e) {
            return false;
        }
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}