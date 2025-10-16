package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AppGUI extends JFrame {
    private final PessoaDAO pessoaDAO;
    private JTextField nomeField, idadeField, buscaField;
    private JTextArea resultadoArea;
    
    public AppGUI() {
        this.pessoaDAO = new PessoaDAO();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Gerenciador de Pessoas - " + ConfigManager.getServerName());
        // JFrame.EXIT_ON_CLOSE é o correto para a janela principal
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        createMenuBar();
        createMainPanel();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu configMenu = new JMenu("Configuração");
        JMenuItem configItem = new JMenuItem("Reconfigurar Banco de Dados");
        configItem.addActionListener(e -> reconfigureDatabase());
        configMenu.add(configItem);
        
        menuBar.add(configMenu);
        setJMenuBar(menuBar);
    }
    
    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(createInputPanel(), BorderLayout.NORTH);
        mainPanel.add(createSearchPanel(), BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        panel.add(new JLabel("Nome:"));
        nomeField = new JTextField(15);
        panel.add(nomeField);
        
        panel.add(new JLabel("Idade:"));
        idadeField = new JTextField(5);
        panel.add(idadeField);
        
        JButton salvarButton = new JButton("Salvar");
        salvarButton.addActionListener(e -> salvarPessoa());
        panel.add(salvarButton);
        
        return panel;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar:"));
        buscaField = new JTextField(20);
        searchPanel.add(buscaField);
        
        JButton buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarPessoa());
        searchPanel.add(buscarButton);
        
        resultadoArea = new JTextArea(15, 50);
        resultadoArea.setEditable(false);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultadoArea), BorderLayout.CENTER);
        
        return panel;
    }
    
    private void salvarPessoa() {
        try {
            String nome = nomeField.getText().trim();
            String idadeText = idadeField.getText().trim();
            
            if (nome.isEmpty()) {
                showError("O nome não pode ser vazio.");
                return;
            }
            
            if (idadeText.isEmpty()) {
                showError("A idade não pode ser vazia.");
                return;
            }
            
            int idade = Integer.parseInt(idadeText);
            
            pessoaDAO.salvar(nome, idade);
            
            nomeField.setText("");
            idadeField.setText("");
            
            JOptionPane.showMessageDialog(this, "Pessoa salva com sucesso!", 
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            showError("A idade deve ser um número válido.");
        } catch (Exception e) {
            showError("Erro ao salvar pessoa: " + e.getMessage());
        }
    }
    
    private void buscarPessoa() {
        try {
            String termo = buscaField.getText().trim();
            List<String> resultados = pessoaDAO.buscar(termo);
            
            resultadoArea.setText("");
            if (resultados.isEmpty()) {
                resultadoArea.setText("Nenhum resultado encontrado.");
            } else {
                resultados.forEach(resultado -> resultadoArea.append(resultado + "\n"));
            }
        } catch (Exception e) {
            showError("Erro ao buscar: " + e.getMessage());
        }
    }
    
    private void reconfigureDatabase() {
        int option = JOptionPane.showConfirmDialog(this,
            "Deseja reconfigurar o banco de dados? O aplicativo será reiniciado.",
            "Reconfigurar", JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            ConfigManager.setProperty("db.server", "");
            dispose(); // Fecha a janela AppGUI atual
            
            // Abre o SetupDialog
            SwingUtilities.invokeLater(() -> {
                SetupDialog setupDialog = new SetupDialog();
                setupDialog.setVisible(true);
                
                // O AppGUI precisa de um listener para saber quando o SetupDialog fechar
                setupDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        checkAndOpenMainApp();
                    }
                });
            });
        }
    }
    
    // MÉTODO UNIFICADO E CORRIGIDO
    private void checkAndOpenMainApp() {
        // Verifica se o banco está configurado após a reconfiguração
        if (isDatabaseConfigured()) {
            SwingUtilities.invokeLater(() -> {
                // Chama o Main para garantir o fluxo de limpeza de janelas antes de reabrir.
                Main.launchMainApplication(); 
            });
        } else {
            // Se não estiver configurado, volta para a escolha inicial
            SwingUtilities.invokeLater(() -> {
                Main.showInitialChoiceDialog();
            });
        }
    }
    
    // REMOVIDO: O método checkAndOpenMainApp() duplicado
    
    private boolean isDatabaseConfigured() {
        String serverName = ConfigManager.getServerName();
        if (serverName == null || serverName.trim().isEmpty()) {
            return false;
        }
        
        boolean serverConnected = ConnectionFactory.testConnection(serverName);
        if (!serverConnected) {
            return false;
        }
        
        boolean databaseExists = DatabaseManager.getInstance().databaseExists(serverName);
        return databaseExists;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}