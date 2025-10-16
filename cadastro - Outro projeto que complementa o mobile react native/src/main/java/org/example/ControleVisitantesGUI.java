package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

// Assume que estas classes estão disponíveis no classpath
import org.example.ConfigManager;
import org.example.Visitante;
import org.example.ConnectionFactory; 
import org.example.HistoricoDAO;       

public class ControleVisitantesGUI extends JFrame {
    private final DatabaseService dbService;
    private JTextField placaField, nomeField, sobrenomeField, buscaField;
    private JComboBox<String> categoriaField, proprietarioField; 
    private JTextField enderecoBuscaField; // CAMPO UNIFICADO DE ENDEREÇO
    private JTextArea resultadoArea;

    public ControleVisitantesGUI() {
        this.dbService = DatabaseService.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Sistema de Controle de Visitantes - EditPro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        createMenuBar();
        createMainPanel();
        loadDadosIniciais(); 
        
        setupF8Action(); 
        setupF2Action(); 
    }

    private void setupF8Action() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        KeyStroke f8Key = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        
        inputMap.put(f8Key, "registerAction");
        actionMap.put("registerAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarEntradaCompleta();
            }
        });
    }
    
    private void setupF2Action() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        KeyStroke f2Key = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        
        inputMap.put(f2Key, "clearAction");
        actionMap.put("clearAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limparCampos(); // Chama o método de limpeza
            }
        });
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu configMenu = new JMenu("Configuração");
        JMenuItem configItem = new JMenuItem("Reconfigurar Banco de Dados");
        configItem.addActionListener(e -> reconfigureDatabase());
        configMenu.add(configItem);

        JMenuItem debugItem = new JMenuItem("🔧 Debug Banco");
        debugItem.addActionListener(e -> verificarEstruturaBanco());
        configMenu.add(debugItem);

        JMenuItem sairItem = new JMenuItem("Sair");
        sairItem.addActionListener(e -> System.exit(0));
        configMenu.add(sairItem);

        menuBar.add(configMenu);
        setJMenuBar(menuBar);
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel buscaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buscaPanel.add(new JLabel("🔍 Buscar por Placa:"));
        
        buscaField = new JTextField(15);
        
        // Aplica o filtro de maiúsculas ao campo de busca
        buscaField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (Character.isLetter(keyChar) && Character.isLowerCase(keyChar)) {
                    e.setKeyChar(Character.toUpperCase(keyChar));
                }
            }
        });
        
        buscaPanel.add(buscaField);

        JButton buscarButton = new JButton("Buscar Visitante");
        buscarButton.addActionListener(e -> buscarVisitante());
        buscaPanel.add(buscarButton);

        JButton historicoButton = new JButton("Ver Histórico Completo");
        historicoButton.addActionListener(e -> verHistorico());
        buscaPanel.add(historicoButton);

        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton listarButton = new JButton("📋 Listar Todos");
        listarButton.addActionListener(e -> listarVisitantes());
        botoesPanel.add(listarButton);

        JButton estatisticasButton = new JButton("📊 Estatísticas");
        estatisticasButton.addActionListener(e -> mostrarEstatisticas());
        botoesPanel.add(estatisticasButton);

        resultadoArea = new JTextArea(20, 80);
        resultadoArea.setEditable(false);
        resultadoArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultadoArea);

        panel.add(buscaPanel, BorderLayout.NORTH);
        panel.add(botoesPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainPanel.add(createInputPanel(), BorderLayout.NORTH);
        mainPanel.add(createSearchPanel(), BorderLayout.CENTER); 

        add(mainPanel);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel tituloLabel = new JLabel("🎯 REGISTRAR NOVA ENTRADA");
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 16));
        tituloLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel camposPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<String> categorias = dbService.buscarCategorias();
        if (categorias == null) categorias = List.of("Visitante");
        
        List<String> proprietarios = dbService.buscarProprietarios(); 
        if (proprietarios == null) proprietarios = List.of("Nenhum");


        // Linha 1: Placa e Categoria
        gbc.gridx = 0; gbc.gridy = 0; camposPanel.add(new JLabel("Placa do Veículo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; 
        placaField = new JTextField(12); 
        placaField.addKeyListener(new KeyAdapter() { // LISTENER AGORA CHAMA APENAS O AUTOCOMPLETAR
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    autocompletarVisitante(placaField.getText());
                }
            }
            // Aplica filtro de maiúsculas
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (Character.isLetter(keyChar) && Character.isLowerCase(keyChar)) {
                    e.setKeyChar(Character.toUpperCase(keyChar));
                }
            }
        });
        camposPanel.add(placaField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; camposPanel.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; 
        categoriaField = new JComboBox<>(categorias.toArray(new String[0])); 
        
        // CORREÇÃO CRÍTICA: Definir a categoria padrão para "Visitante"
        int targetIndex = 0;
        for (int i = 0; i < categoriaField.getItemCount(); i++) {
            if (categoriaField.getItemAt(i).equalsIgnoreCase("Visitante")) {
                targetIndex = i; // Encontrou "Visitante"
                break;
            }
        }
        
        // Aplica o índice (0, se "Visitante" não foi encontrado, ou o índice de "Visitante")
        if (categoriaField.getItemCount() > 0) {
            categoriaField.setSelectedIndex(targetIndex); 
        }
        
        camposPanel.add(categoriaField, gbc);

        // Linha 2: Nome, Sobrenome
        gbc.gridx = 0; gbc.gridy = 1; camposPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; 
        nomeField = new JTextField(15); 
        nomeField.addKeyListener(new KeyAdapter() { // NOVO KEYADAPTER CAPS LOCK
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (Character.isLetter(keyChar) && Character.isLowerCase(keyChar)) {
                    e.setKeyChar(Character.toUpperCase(keyChar));
                }
            }
        });
        camposPanel.add(nomeField, gbc);

        gbc.gridx = 2; gbc.gridy = 1; camposPanel.add(new JLabel("Sobrenome:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; 
        sobrenomeField = new JTextField(15); 
        sobrenomeField.addKeyListener(new KeyAdapter() { // NOVO KEYADAPTER CAPS LOCK
            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (Character.isLetter(keyChar) && Character.isLowerCase(keyChar)) {
                    e.setKeyChar(Character.toUpperCase(keyChar));
                }
            }
        });
        camposPanel.add(sobrenomeField, gbc);

        // Linha 3: Endereço (Campo agora é obrigatório apenas para PLACAS NOVAS)
        gbc.gridx = 0; gbc.gridy = 2; camposPanel.add(new JLabel("Endereço/Número:"), gbc);
        
        // CAMPO UNIFICADO DE BUSCA DE ENDEREÇO
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; // Ocupa 3 colunas
        enderecoBuscaField = new JTextField(30); 
        enderecoBuscaField.addKeyListener(new KeyAdapter() { // LISTENER PARA BUSCA INTELIGENTE
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarEndereco(enderecoBuscaField.getText());
                }
            }
        });
        camposPanel.add(enderecoBuscaField, gbc);
        gbc.gridwidth = 1; // Reseta o layout

        // CAMPO PROPRIETÁRIO
        gbc.gridx = 4; gbc.gridy = 2; camposPanel.add(new JLabel("Proprietário:"), gbc);
        gbc.gridx = 5; gbc.gridy = 2; 
        proprietarioField = new JComboBox<>(proprietarios.toArray(new String[0])); 
        proprietarioField.setEnabled(false); // Desabilita, pois a lógica de registro irá buscar
        camposPanel.add(proprietarioField, gbc);

        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton registrarButton = new JButton("🚗 Registrar Entrada (F8)"); // Adicionando F8 no label
        registrarButton.addActionListener(e -> registrarEntradaCompleta());
        registrarButton.setBackground(new Color(34, 139, 34));
        registrarButton.setForeground(Color.WHITE);
        registrarButton.setFont(new Font("Arial", Font.BOLD, 12));
        botoesPanel.add(registrarButton);

        JButton limparButton = new JButton("🗑️ Limpar Campos (F2)"); // Adicionando F2 no label
        limparButton.addActionListener(e -> limparCampos());
        botoesPanel.add(limparButton);

        panel.add(tituloLabel, BorderLayout.NORTH);
        panel.add(camposPanel, BorderLayout.CENTER);
        panel.add(botoesPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== MÉTODOS DE AUTOMAÇÃO E REGISTRO ====================
    
    // CORRIGIDO: Preenche a Categoria do histórico e não a padrão.
    private void autocompletarVisitante(String placa) {
        Visitante v = dbService.buscarVisitantePorDocumento(placa.trim().toUpperCase());
        
        if (v != null) {
            // PLACA CONHECIDA: Preenche Nome, Sobrenome, Endereço e CATEGORIA.
            
            String nome = v.getNome();
            String sobrenome = v.getSobrenome();
            // CHAVE: Busca a categoria do último histórico (Regra: Placa existente usa última categoria)
            String categoria = dbService.buscarNomeCategoria(v.getDocumento()); 
            String ultimoEndereco = dbService.buscarUltimoEndereco(placa);
            
            nomeField.setText(nome);
            sobrenomeField.setText(sobrenome != null ? sobrenome : "");
            enderecoBuscaField.setText(ultimoEndereco);
            
            // Define a categoria no ComboBox (busca pelo nome)
            for (int i = 0; i < categoriaField.getItemCount(); i++) {
                if (categoriaField.getItemAt(i).equalsIgnoreCase(categoria)) {
                    categoriaField.setSelectedIndex(i);
                    break;
                }
            }
            
        } else {
            // PLACA NOVA: Prepara para registro manual (Mantém a categoria no valor inicial 'Visitante')
            
            // Limpa para forçar a entrada de dados
            nomeField.setText("");
            sobrenomeField.setText("");
            enderecoBuscaField.setText("");
            
            nomeField.requestFocus();
        }
    }

    private void buscarEndereco(String busca) {
        if (busca.isEmpty()) return;

        List<String> enderecos = dbService.buscarEnderecos(busca);

        if (enderecos.isEmpty()) {
            showError("Nenhum endereço encontrado para: " + busca);
            return;
        }

        // Mostra a lista de endereços para escolha
        String[] options = enderecos.toArray(new String[0]);
        String selectedAddress = (String) JOptionPane.showInputDialog(
            this,
            "Selecione o endereço de destino:",
            "Escolha de Endereço",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (selectedAddress != null) {
            enderecoBuscaField.setText(selectedAddress);
        }
    }

    private void registrarEntradaCompleta() {
        // Método para registro manual OU após autocompletar
        try {
            String placa = placaField.getText().trim().toUpperCase();
            String nome = nomeField.getText().trim();
            String sobrenome = sobrenomeField.getText().trim();
            String categoria = (String) categoriaField.getSelectedItem();
            
            String enderecoCompleto = enderecoBuscaField.getText();
            
            // CRÍTICO: Quebramos o endereço Setor/Unidade do formato "Setor/Unidade"
            String setor, unidade;
            if (enderecoCompleto.contains("/")) {
                String[] partes = enderecoCompleto.split("/", 2);
                setor = partes[0].trim();
                unidade = partes[1].trim();
            } else {
                setor = enderecoCompleto.trim();
                unidade = "N/A"; 
            }
            
            String proprietario = "AUTO_BUSCAR"; // Flag para acionar a busca do proprietário real


            // Validação
            if (placa.isEmpty() || nome.isEmpty() || enderecoCompleto.isEmpty()) {
                showError("Preencha os campos obrigatórios: Placa, Nome e Endereço!");
                return;
            }

            // 1. Verifica/Atualiza Visitante
            Visitante existente = dbService.buscarVisitantePorDocumento(placa);
            boolean isNovoVisitante = (existente == null);
            boolean sucessoVisitante;
            
            // Se for novo, salva. Se for existente, apenas atualiza a visita.
            if (isNovoVisitante) {
                sucessoVisitante = dbService.salvarVisitante(placa, nome, sobrenome, categoria);
            } else {
                sucessoVisitante = dbService.atualizarVisitas(placa);
            }
            
            // 2. Registra histórico e destino 
            int codigoHistorico = dbService.registrarEntradaHistorico(placa, nome, sobrenome, categoria);
            
            if (codigoHistorico == -1) {
                showError("Falha crítica ao registrar o histórico. Abortando registro de destino.");
                return;
            }
            
            // O DatabaseService usa a lógica CORRIGIDA para buscar o proprietário real
            dbService.registrarDestino(placa, setor, unidade, proprietario, codigoHistorico);
            
            // ... (Mensagens de sucesso)
             if (sucessoVisitante) {
                if (!isNovoVisitante) {
                    // Busca novamente para obter o número atualizado de visitas
                    existente = dbService.buscarVisitantePorDocumento(placa);
                }

                String logMessage = isNovoVisitante ?
                    "✅ NOVO VISITANTE REGISTRADO: Placa " + placa :
                    "✅ ENTRADA REGISTRADA: Placa " + placa + ", Visita #" + (existente != null ? (existente.getNumeroVisitas() + 1) : "N/A");
                
                System.out.println(logMessage); // Logando a mensagem para o console
                limparCampos();
            } else {
                showError("Falha crítica ao salvar/atualizar o registro do visitante.");
            }

        } catch (Exception e) {
            showError("Falha crítica ao salvar/atualizar o registro do visitante: " + e.getMessage());
            System.err.println("ERRO CRÍTICO NO REGISTRO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void buscarVisitante() {
        String placa = buscaField.getText().trim().toUpperCase();
        if (placa.isEmpty()) {
            showError("Digite uma placa para buscar.");
            return;
        }

        try {
            // CORREÇÃO CRÍTICA: Chama o método que busca MÚLTIPLOS resultados parciais
            List<Visitante> resultados = dbService.buscarVisitantesPorDocumentoParcial(placa);
            resultadoArea.setText("");
            
            if (resultados.isEmpty()) {
                resultadoArea.append("❌ NENHUM VISITANTE ENCONTRADO COM A PLACA: " + placa + "\n");
                resultadoArea.append("💡 Dica: Registre uma nova entrada para este veículo.\n");
                return;
            }
            
            if (resultados.size() == 1) {
                // Se for apenas um, exibe o histórico detalhado, como antes
                Visitante v = resultados.get(0);
                resultadoArea.append("=== 🚗 VISITANTE ENCONTRADO ===\n\n");
                resultadoArea.append("📋 PLACA: " + v.getDocumento() + "\n");
                resultadoArea.append("👤 NOME: " + v.getNome() + " " + v.getSobrenome() + "\n");
                resultadoArea.append("🔢 VISITAS: " + v.getNumeroVisitas() + "\n");
                resultadoArea.append("📅 CADASTRO: " + v.getDataHoraCadastro() + "\n\n");
                
                // Busca histórico completo (usa o documento completo encontrado)
                resultadoArea.append("=== 📊 HISTÓRICO DE VISITAS (Entrada | Endereço | Categoria | Proprietário) ===\n\n");
                
                HistoricoDAO dao = new HistoricoDAO();
                List<String> historico = dao.buscarHistoricoCompletoPorDocumento(v.getDocumento());
                
                if (historico.isEmpty()) {
                    resultadoArea.append("Nenhum registro de visita encontrado.\n");
                } else {
                    historico.forEach(registro -> resultadoArea.append("• " + registro + "\n"));
                    resultadoArea.append("\nTotal de registros: " + historico.size() + "\n");
                }
                
            } else {
                // MÚLTIPLOS RESULTADOS: Exibe todos os matches na área de texto
                resultadoArea.append("=== 🎯 MÚLTIPLOS VISITANTES ENCONTRADOS (" + resultados.size() + ") ===\n\n");
                resultadoArea.append(String.format("%-10s | %-20s | %s\n", "PLACA", "NOME COMPLETO", "VISITAS"));
                resultadoArea.append("-".repeat(45) + "\n");
                
                for (Visitante v : resultados) {
                    String nomeCompleto = v.getNome() + " " + (v.getSobrenome() != null ? v.getSobrenome() : "");
                    // Limita o nome completo para caber na tabela de exibição
                    if (nomeCompleto.length() > 20) {
                        nomeCompleto = nomeCompleto.substring(0, 17) + "...";
                    }
                    
                    resultadoArea.append(String.format("%-10s | %-20s | %d\n", 
                        v.getDocumento(), 
                        nomeCompleto, 
                        v.getNumeroVisitas()
                    ));
                }
                resultadoArea.append("\n💡 Digite a placa completa na caixa de busca para ver o histórico detalhado.\n");
            }

        } catch (Exception e) {
            showError("Erro ao buscar visitante: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void listarVisitantes() {
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TOP 50000 Documento, Nome, SobreNome, NumeroVisitas, DataHora " +
                     "FROM Visitantes ORDER BY DataHora DESC")) {

            resultadoArea.setText("=== 🚗 TODO OS VISITANTES ===\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                resultadoArea.append(String.format("%d. %s - %s %s (%d visitas) - %s\n",
                    count,
                    rs.getString("Documento"),
                    rs.getString("Nome"),
                    rs.getString("SobreNome") != null ? rs.getString("SobreNome") : "",
                    rs.getInt("NumeroVisitas"),
                    rs.getTimestamp("DataHora")
                ));
            }
            if (count == 0) {
                resultadoArea.append("Nenhum visitante encontrado.\n");
            } else {
                resultadoArea.append("\n📊 Total: " + count + " visitantes encontrados.\n");
            }

        } catch (Exception e) {
            showError("Erro ao listar visitantes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Variável de membro para o diálogo de loading
    private JDialog loadingDialog;

    private void showLoadingDialog(String message) {
        // Cria e exibe um diálogo não modal na Thread de Despacho de Eventos (EDT)
        SwingUtilities.invokeLater(() -> {
            loadingDialog = new JDialog(this, "Processando...", false); // false = não modal
            loadingDialog.setSize(300, 100);
            loadingDialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            panel.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.NORTH);
            panel.add(new JProgressBar(0, 100) {{ setIndeterminate(true); }}, BorderLayout.CENTER);

            loadingDialog.add(panel);
            loadingDialog.setVisible(true);
        });
    }

    private void hideLoadingDialog() {
        // Garante que o diálogo seja fechado na EDT
        if (loadingDialog != null) {
            SwingUtilities.invokeLater(() -> {
                loadingDialog.dispose();
                loadingDialog = null;
            });
        }
    }

    private void mostrarEstatisticas() {
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            resultadoArea.setText("=== 📊 ESTATÍSTICAS DO SISTEMA ===\n\n");

            // Total de visitantes
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Visitantes");
            if (rs.next()) resultadoArea.append("👥 Total de visitantes: " + rs.getInt("total") + "\n");

            // Total de entradas registradas
            rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Historicos");
            if (rs.next()) resultadoArea.append("📝 Entradas registradas: " + rs.getInt("total") + "\n");

            // Total de destinos registrados
            rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM HistoricosVisitados");
            if (rs.next()) resultadoArea.append("🎯 Destinos registrados: " + rs.getInt("total") + "\n");

            // Visitas do dia
            rs = stmt.executeQuery(
                "SELECT COUNT(*) AS hoje FROM Historicos WHERE CAST(DataHoraEntrada AS DATE) = CAST(GETDATE() AS DATE)");
            if (rs.next()) resultadoArea.append("📅 Visitas hoje: " + rs.getInt("hoje") + "\n");

            // Visitante mais frequente
            rs = stmt.executeQuery(
                "SELECT TOP 1 Documento, Nome, NumeroVisitas FROM Visitantes ORDER BY NumeroVisitas DESC");
            if (rs.next()) {
                resultadoArea.append("🏆 Visitante mais frequente: " +
                    rs.getString("Documento") + " - " + rs.getString("Nome") +
                    " (" + rs.getInt("NumeroVisitas") + " visitas)\n");
            }

        } catch (Exception e) {
            showError("Erro ao gerar estatísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void verHistorico() {
        // Usa o método buscarTodosHistoricos do DAO para listagem geral
        HistoricoDAO dao = new HistoricoDAO();
        List<String> historico = dao.buscarTodosHistoricos();

        resultadoArea.setText("=== 📋 HISTÓRICO COMPLETO (ÚLTIMOS 50) ===\n\n");
        int count = 0;
        if (historico.isEmpty()) {
             resultadoArea.append("Nenhum registro no histórico.\n");
        } else {
            for (String registro : historico) {
                count++;
                resultadoArea.append(String.format("%d. %s\n", count, registro));
            }
             resultadoArea.append("\n📊 Total: " + count + " visitantes\n");
        }
    }

    private void verificarEstruturaBanco() {
        resultadoArea.setText("=== 🗄️ ESTRUTURA DO BANCO EDITPRO ===\n\n");
        String[] tabelas = {"Visitantes", "Historicos", "HistoricosVisitados", "Setores", "Unidades", "Visitados", "CategoriasVisitantes"};

        for (String tabela : tabelas) {
            resultadoArea.append("=== " + tabela + " ===\n");
            try { 
                if (dbService.tableExists(tabela)) {
                    List<String> colunas = dbService.getColumnNames(tabela);
                    if (colunas.isEmpty()) {
                        resultadoArea.append("✅ Tabela existe, mas sem colunas listadas.\n");
                    } else {
                        colunas.forEach(c -> resultadoArea.append("    • " + c + "\n"));
                    }
                } else {
                    resultadoArea.append("❌ Tabela não encontrada.\n");
                }
            } catch (Exception e) {
                 resultadoArea.append("❌ ERRO ao acessar estrutura: " + e.getMessage() + "\n");
            }
            resultadoArea.append("\n");
        }
    }

    private void limparCampos() {
        placaField.setText("");
        nomeField.setText("");
        sobrenomeField.setText("");
        enderecoBuscaField.setText("");
        
        // NOVO: Limpa o campo de busca também
        buscaField.setText("");
        
        // Reset da Categoria para o padrão "VISITANTE"
        int targetIndex = 0;
        for (int i = 0; i < categoriaField.getItemCount(); i++) {
            if (categoriaField.getItemAt(i).equalsIgnoreCase("Visitante")) {
                targetIndex = i; // Encontrou "Visitante"
                break;
            }
        }
        if (categoriaField.getItemCount() > 0) {
            categoriaField.setSelectedIndex(targetIndex); 
        }
        
        try {
            if (proprietarioField != null && proprietarioField.getItemCount() > 0) {
                proprietarioField.setSelectedIndex(0); 
            }
        } catch (Exception e) {
            // Ignora o erro de inicialização
        }
        
        placaField.requestFocus();
    }

    private void loadDadosIniciais() {
        resultadoArea.setText("=== SISTEMA de CONTROLE de VISITANTES ===\n");
        resultadoArea.append("Banco: EditPro\n");
        resultadoArea.append("Servidor: " + ConfigManager.getServerName() + "\n\n");
        resultadoArea.append("💡 Use os botões para registrar e buscar visitantes.\n");
    }

    private void reconfigureDatabase() {
        int opt = JOptionPane.showConfirmDialog(this,
            "Deseja reconfigurar o banco de dados?\n\nIsso fechará a aplicação atual.",
            "Reconfigurar Banco", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            ConfigManager.setProperty("db.server", "");
            dispose();
            // Assume que Main::showInitialChoiceDialog existe
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "❌ Erro", JOptionPane.ERROR_MESSAGE);
    }
}