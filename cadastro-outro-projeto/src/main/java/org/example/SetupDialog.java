package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class SetupDialog extends JDialog {
    private JTextField serverField;
    private JButton detectButton, testButton, okButton, cancelButton, installButton, gerenciarDbButton;
    private JTextArea logArea;
    private boolean configured = false;
    
    public SetupDialog() {
        super((Frame) null, "Configuração do Banco de Dados - Detecção Inteligente", true);
        initializeUI();
    }
    
    // REMOVIDO: Construtor problemático que lança exceção
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Painel de entrada
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Servidor SQL:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        serverField = new JTextField(ConfigManager.getServerName(), 25);
        inputPanel.add(serverField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        detectButton = new JButton("🔍 Detectar Automaticamente");
        detectButton.addActionListener(this::detectServers);
        inputPanel.add(detectButton, gbc);
        
        // Área de log
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setBackground(new Color(240, 240, 240));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        
        // Painel de botões de ação
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        installButton = new JButton("📋 Instalar SQL Server Express");
        installButton.addActionListener(this::showInstallationHelp);
        actionPanel.add(installButton);
        
        gerenciarDbButton = new JButton("🗃️ Gerenciar Banco de Dados");
        gerenciarDbButton.addActionListener(e -> {
            dispose();
            new InstallerGUI().setVisible(true);
        });
        actionPanel.add(gerenciarDbButton);
        
        // Painel de controle
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        testButton = new JButton("🧪 Testar Conexão");
        testButton.addActionListener(this::testConnection);
        controlPanel.add(testButton);
        
        okButton = new JButton("✅ Salvar e Conectar");
        okButton.addActionListener(this::saveConfiguration);
        controlPanel.add(okButton);
        
        cancelButton = new JButton("❌ Cancelar");
        cancelButton.addActionListener(e -> dispose());
        controlPanel.add(cancelButton);
        
        // Montagem do layout
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(logScroll, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(okButton);
        
        // Detecção automática ao abrir
        SwingUtilities.invokeLater(this::autoDetectOnStart);
    }
    
    private void autoDetectOnStart() {
        logMessage("🚀 Iniciando detecção automática de instâncias SQL Server...");
        detectServers(null);
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void detectServers(ActionEvent e) {
        logMessage("\n=== INICIANDO DETECÇÃO DE INSTÂNCIAS ===");
        
        new Thread(() -> {
            try {
                List<String> instances = DatabaseManager.getInstance().detectAllPossibleInstances();
                
                SwingUtilities.invokeLater(() -> {
                    if (!instances.isEmpty()) {
                        logMessage("✅ " + instances.size() + " instância(s) detectada(s)!");
                        
                        String[] serverArray = instances.toArray(new String[0]);
                        String selected = (String) JOptionPane.showInputDialog(
                            this, 
                            "Instâncias SQL Server detectadas:\n\n" +
                            "💡 Dica: Use 'localhost\\\\SQLEXPRESS' ou '.\\\\SQLEXPRESS'",
                            "Detecção Automática",
                            JOptionPane.QUESTION_MESSAGE, 
                            null, 
                            serverArray, 
                            serverArray[0]
                        );
                        
                        if (selected != null) {
                            serverField.setText(selected);
                            logMessage("📋 Instância selecionada: " + selected);
                            testConnection(null);
                        }
                    } else {
                        logMessage("❌ Nenhuma instância conectável encontrada.");
                        suggestSolutions();
                    }
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    logMessage("❌ Erro durante detecção: " + ex.getMessage());
                });
            }
        }).start();
    }
    
    private void suggestSolutions() {
        int option = JOptionPane.showConfirmDialog(this,
            "Nenhuma instância SQL Server foi detectada.\n\n" +
            "Possíveis soluções:\n" +
            "• Verifique se o SQL Server está rodando\n" +
            "• Habilite TCP/IP no SQL Server Configuration Manager\n\n" +
            "Deseja ver instruções detalhadas?",
            "Configuração Necessária", 
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            showDetailedInstallationHelp();
        }
    }
    
    private void showDetailedInstallationHelp() {
        String message = 
            "🔧 SOLUÇÕES PARA PROBLEMAS DE CONEXÃO:\n\n" +
            "1. VERIFICAR SERVIÇO SQL SERVER:\n" +
            "   - Abra 'Services.msc'\n" +
            "   - Procure por 'SQL Server (SQLEXPRESS)'\n" +
            "   - Se parado, clique com botão direito e selecione 'Iniciar'\n\n" +
            "2. HABILITAR TCP/IP:\n" +
            "   - Abra 'SQL Server Configuration Manager'\n" +
            "   - Vá em 'SQL Server Network Configuration' > 'Protocols for SQLEXPRESS'\n" +
            "   - Clique com botão direito em 'TCP/IP' e selecione 'Enable'\n" +
            "   - Reinicie o serviço SQL Server\n\n" +
            "3. INSTALAÇÃO SQL SERVER EXPRESS:\n" +
            "   - Download: https://www.microsoft.com/en-us/sql-server/sql-server-downloads\n" +
            "   - Escolha 'Basic' durante a instalação";
            
        JTextArea textArea = new JTextArea(message, 15, 60);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Soluções para Conexão SQL Server", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showInstallationHelp(ActionEvent e) {
        String message = 
            "📋 INSTRUÇÕES PARA INSTALAR SQL SERVER EXPRESS:\n\n" +
            "1. Baixe o SQL Server Express gratuitamente:\n" +
            "   https://www.microsoft.com/en-us/sql-server/sql-server-downloads\n\n" +
            "2. Durante a instalação:\n" +
            "   - Tipo de instalação: Básica\n" +
            "   - Aceite a licença\n" +
            "   - Instale com configurações padrão";
            
        JTextArea textArea = new JTextArea(message, 10, 50);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Instalação do SQL Server Express", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void testConnection(ActionEvent e) {
        String serverName = serverField.getText().trim();
        
        if (serverName.isEmpty()) {
            showError("Digite o nome do servidor SQL Server.");
            return;
        }
        
        logMessage("\n=== TESTANDO CONEXÃO ===");
        logMessage("Servidor: " + serverName);
        
        new Thread(() -> {
            try {
                boolean connected = ConnectionFactory.testConnection(serverName);
                
                SwingUtilities.invokeLater(() -> {
                    if (connected) {
                        logMessage("✅ CONEXÃO BEM-SUCEDIDA!");
                        JOptionPane.showMessageDialog(this,
                            "✅ Conexão estabelecida com sucesso!\n" +
                            "Servidor: " + serverName,
                            "Teste de Conexão", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        logMessage("❌ FALHA NA CONEXÃO");
                        showError("Não foi possível conectar ao servidor.\n" +
                                 "Verifique se o SQL Server está rodando.");
                    }
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    logMessage("❌ Erro no teste: " + ex.getMessage());
                    showError("Erro ao testar conexão: " + ex.getMessage());
                });
            }
        }).start();
    }
    
    private void saveConfiguration(ActionEvent e) {
        String serverName = serverField.getText().trim();
        
        if (serverName.isEmpty()) {
            showError("O nome do servidor é obrigatório.");
            return;
        }
        
        logMessage("\n=== SALVANDO CONFIGURAÇÃO ===");
        
        new Thread(() -> {
            try {
                // Testa APENAS a conexão com o servidor (NÃO com o banco)
                logMessage("🔌 Testando conexão com servidor...");
                boolean serverConnected = ConnectionFactory.testConnection(serverName);
                
                if (!serverConnected) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Não foi possível conectar ao servidor.\nTeste a conexão antes de salvar.");
                    });
                    return;
                }
                
                logMessage("✅ Conexão com servidor bem-sucedida!");
                
                // Salva a configuração do servidor
                ConfigManager.setServerName(serverName);
                logMessage("💾 Configuração salva: " + serverName);
                
                // PERGUNTA AO USUÁRIO O QUE DESEJA FAZER
                SwingUtilities.invokeLater(() -> {
                    Object[] options = {
                        "🗃️ Criar Novo Banco EditPro", 
                        "💾 Restaurar de Backup", 
                        "🚪 Só Salvar Configuração"
                    };
                    
                    int choice = JOptionPane.showOptionDialog(this,
                        "Configuração do servidor salva com sucesso!\n\n" +
                        "O que deseja fazer agora?\n" +
                        "• 🗃️ Criar Novo Banco: Cria banco EDITPRO e TODAS as tabelas\n" +
                        "• 💾 Restaurar Backup: Restaura de arquivo .bak\n" +
                        "• 🚪 Só Salvar: Apenas salva a configuração",
                        "Escolha uma Ação",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                    
                    switch (choice) {
                        case 0: // Criar Banco
                            createDatabase(serverName);
                            break;
                        case 1: // Restaurar Backup
                            restoreBackup(serverName);
                            break;
                        case 2: // Só Salvar
                            finishConfiguration();
                            break;
                        default:
                            finishConfiguration();
                    }
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    logMessage("❌ Erro: " + ex.getMessage());
                    showError("Erro na configuração: " + ex.getMessage());
                });
            }
        }).start();
    }
    
    private void restoreBackup(String serverName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivo de Backup (.bak)");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Backup SQL Server", "bak"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();
            
            new Thread(() -> {
                try {
                    logMessage("💾 Iniciando restauração do backup...");
                    logMessage("Arquivo: " + backupFile.getAbsolutePath());
                    
                    DatabaseInitializer initializer = new DatabaseInitializer();
                    boolean success = initializer.restoreDatabase(backupFile.getAbsolutePath());
                    
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            logMessage("✅ Backup restaurado com sucesso!");
                            finishConfiguration();
                        } else {
                            logMessage("❌ Falha ao restaurar backup");
                            showError("Falha ao restaurar backup. Tente novamente.");
                        }
                    });
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        logMessage("❌ Erro na restauração: " + ex.getMessage());
                        showError("Erro ao restaurar backup: " + ex.getMessage());
                    });
                }
            }).start();
        }
    }
    
    private void createDatabase(String serverName) {
        new Thread(() -> {
            try {
                logMessage("🗃️ Criando banco de dados EDITPRO...");
                
                boolean success = ConnectionFactory.createDatabaseIfNotExists(serverName);
                
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        logMessage("✅ Banco EDITPRO criado com sucesso!");
                        logMessage("✅ Todas as tabelas foram criadas automaticamente!");
                        finishConfiguration();
                    } else {
                        showError("Falha ao criar banco de dados EditPro.");
                    }
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    logMessage("❌ Erro na criação: " + ex.getMessage());
                    showError("Erro ao criar banco EditPro: " + ex.getMessage());
                });
            }
        }).start();
    }
    
    private void finishConfiguration() {
        configured = true;
        logMessage("🎉 Configuração concluída com sucesso!");
        SwingUtilities.invokeLater(() -> dispose());
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    
    public boolean showDialog() {
        setVisible(true);
        return configured;
    }
}