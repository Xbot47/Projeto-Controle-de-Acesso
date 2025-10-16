import { StyleSheet, Dimensions } from 'react-native';

const { width } = Dimensions.get('window');

export default StyleSheet.create({
  // =============================================
  //  LAYOUT PRINCIPAL
  // =============================================
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollContainer: {
    flexGrow: 1,
    padding: 16,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
    color: '#2c3e50',
  },

  // =============================================
  //  HEADER CUSTOM
  // =============================================
  headerBar: {
    backgroundColor: '#3498db',
    paddingVertical: 12,
    paddingHorizontal: 16,
    paddingTop: 40,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 0,
  },
  headerContent: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
  },
  headerStatus: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
  },
  statusIndicator: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginRight: 8,
  },
  statusTextHeader: {
    color: '#fff',
    fontSize: 14,
  },
  refreshButton: {
    padding: 8,
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 6,
    marginLeft: 10,
  },
  refreshText: {
    color: '#fff',
    fontSize: 18,
  },
  configButton: {
    padding: 8,
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 6,
    marginLeft: 10,
  },
  configText: {
    color: '#fff',
    fontSize: 18,
  },

  // =============================================
  //  STATUS DA HOME
  // =============================================
  statusContainer: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 12,
    marginHorizontal: 16,
    marginTop: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
    marginBottom: 20,
  },
  statusTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginBottom: 15,
  },
  statusRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  statusText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2c3e50',
  },
  statusDetail: {
    fontSize: 14,
    color: '#7f8c8d',
    marginBottom: 4,
    marginLeft: 22,
  },

  // =============================================
  //  FORMULÁRIOS E INPUTS
  // =============================================
  form: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 10,
    marginHorizontal: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
    color: '#2c3e50',
  },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
    fontSize: 16,
    backgroundColor: '#fff',
  },
  
  // =============================================
  //  BOTÕES
  // =============================================
  button: {
    backgroundColor: '#4CAF50',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
    marginVertical: 8,
    elevation: 2,
  },
  buttonDisabled: {
    backgroundColor: '#bdc3c7',
    opacity: 0.6,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  secondaryButton: {
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#3498db',
    marginVertical: 6,
    backgroundColor: 'transparent',
  },
  secondaryButtonText: {
    color: '#3498db',
    fontSize: 16,
    fontWeight: '600',
  },
  smallButton: {
    backgroundColor: '#3498db',
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 12,
    alignSelf: 'center',
    minWidth: 120,
  },
  smallButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
  
  // =============================================
  //  MENU E CARDS
  // =============================================
  menuSection: {
    marginHorizontal: 16,
    marginBottom: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginBottom: 15,
  },
  menuCard: {
    backgroundColor: '#fff',
    padding: 18,
    borderRadius: 12,
    marginBottom: 12,
    borderLeftWidth: 4,
    flexDirection: 'row',
    alignItems: 'center',
    elevation: 2,
  },
  menuIcon: {
    fontSize: 24,
    marginRight: 16,
  },
  menuContent: {
    flex: 1,
  },
  menuTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2c3e50',
    marginBottom: 4,
  },
  menuDescription: {
    fontSize: 14,
    color: '#7f8c8d',
    lineHeight: 18,
  },
  
  // =============================================
  //  CARDS DE HISTÓRICO
  // =============================================
  card: {
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 8,
    marginBottom: 10,
    marginHorizontal: 16,
    borderLeftWidth: 3,
    borderLeftColor: '#3498db',
    elevation: 1,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 6,
    color: '#2c3e50',
  },
  cardText: {
    fontSize: 14,
    color: '#555',
    marginBottom: 3,
  },
  emptyText: {
    textAlign: 'center',
    color: '#95a5a6',
    fontSize: 16,
    marginTop: 50,
  },
  
  // =============================================
  //  NOVOS STYLES PARA AUTOMATIZAÇÃO
  // =============================================
  
  // Card de visitante encontrado
  foundCard: {
    backgroundColor: '#e8f5e8',
    padding: 15,
    borderRadius: 8,
    marginHorizontal: 16,
    marginBottom: 15,
    borderLeftWidth: 4,
    borderLeftColor: '#4CAF50',
  },
  foundTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2e7d32',
    marginBottom: 8,
  },
  foundText: {
    color: '#388e3c',
    marginBottom: 4,
    fontSize: 14,
  },

  // Categorias horizontais
  categoriasScroll: {
    marginBottom: 15,
    maxHeight: 50,
  },
  categoriasContainer: {
    flexDirection: 'row',
    paddingVertical: 5,
  },
  categoriaButton: {
    backgroundColor: '#f1f1f1',
    paddingHorizontal: 15,
    paddingVertical: 8,
    borderRadius: 20,
    marginRight: 8,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  categoriaButtonSelected: {
    backgroundColor: '#3498db',
    borderColor: '#2980b9',
  },
  categoriaText: {
    color: '#666',
    fontSize: 12,
    fontWeight: '500',
  },
  categoriaTextSelected: {
    color: '#fff',
    fontWeight: 'bold',
  },

  // Autocomplete endereços
  suggestionsContainer: {
    position: 'absolute',
    top: 50,
    left: 0,
    right: 0,
    backgroundColor: 'white',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    maxHeight: 150,
    zIndex: 1000,
    elevation: 5,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  suggestionsList: {
    maxHeight: 150,
  },
  suggestionItem: {
    padding: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
    backgroundColor: '#f8f9fa',
    borderRadius: 8,
    marginVertical: 2,
  },
  suggestionText: {
    fontSize: 14,
    color: '#333',
    fontWeight: '500',
  },
  suggestionSubtext: {
    fontSize: 12,
    color: '#7f8c8d',
    marginTop: 2,
  },

  // Texto de loading
  loadingText: {
    fontSize: 12,
    color: '#666',
    fontStyle: 'italic',
    marginTop: 5,
  },

  // Info Box
  infoBox: {
    backgroundColor: '#e3f2fd',
    padding: 15,
    borderRadius: 8,
    marginHorizontal: 16,
    marginBottom: 20,
  },

  // Results List
  resultsList: {
    flex: 1,
    marginTop: 10,
  },
  debugText: {
    fontSize: 12,
    color: '#666',
    fontStyle: 'italic',
    marginTop: 5,
  },

  // =============================================
  //  MODAL STYLES (ATUALIZADOS)
  // =============================================
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalContent: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 20,
    width: '95%',
    maxHeight: '80%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5,
    textAlign: 'center',
    color: '#2c3e50',
  },
  modalSubtitle: {
    fontSize: 14,
    color: '#7f8c8d',
    marginBottom: 15,
    textAlign: 'center',
  },
  modalList: {
    maxHeight: 400,
    marginVertical: 10,
  },
  modalCloseButton: {
    backgroundColor: '#6c757d',
    padding: 12,
    borderRadius: 8,
    marginTop: 10,
    alignItems: 'center',
  },
  modalCloseButtonText: {
    color: 'white',
    textAlign: 'center',
    fontWeight: 'bold',
    fontSize: 16,
  },

  // Input somente leitura
  readOnlyInput: {
    backgroundColor: '#f5f5f5',
    color: '#666',
  },

  // =============================================
  //  STYLES PARA BUSCA INTELIGENTE
  // =============================================
  
  // Sugestões de busca inteligente
  sugestaoItem: {
    padding: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  sugestaoItemPar: {
    backgroundColor: '#f8f9fa',
  },
  sugestaoItemImpar: {
    backgroundColor: '#ffffff',
  },
  sugestaoContent: {
    flex: 1,
  },
  sugestaoPlaca: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#192463',
    marginBottom: 4,
  },
  sugestaoNome: {
    fontSize: 16,
    color: '#333',
    marginBottom: 2,
  },
  sugestaoSobrenome: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  sugestaoVisitas: {
    fontSize: 12,
    color: '#888',
    fontStyle: 'italic',
  },
  sugestoesList: {
    maxHeight: 400,
    width: '100%',
  },

  // Modal específico para busca inteligente
  modalFechar: {
    backgroundColor: '#ff6b6b',
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 10,
  },
  modalFecharTexto: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },

  // Loading container
  loadingContainer: {
    padding: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },

  // =============================================
  //  ESTILOS DE STATUS E CORES
  // =============================================
  successText: {
    color: '#27ae60',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 5,
  },
  errorText: {
    color: '#e74c3c',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 5,
  },
  infoText: {
    color: '#3498db',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 5,
  },
  warningText: {
    color: '#f39c12',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 5,
  },

  // =============================================
  // RESPONSIVIDADE
  // =============================================
  row: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  column: {
    flexDirection: 'column',
  },
  flex1: {
    flex: 1,
  },
  flex2: {
    flex: 2,
  },
  spaceBetween: {
    justifyContent: 'space-between',
  },
  center: {
    justifyContent: 'center',
    alignItems: 'center',
  },

  // =============================================
  //  MARGINS E PADDINGS
  // =============================================
  mt10: { marginTop: 10 },
  mt15: { marginTop: 15 },
  mt20: { marginTop: 20 },
  mb10: { marginBottom: 10 },
  mb15: { marginBottom: 15 },
  mb20: { marginBottom: 20 },
  ml10: { marginLeft: 10 },
  mr10: { marginRight: 10 },
  p10: { padding: 10 },
  p15: { padding: 15 },
  p20: { padding: 20 },

  // =============================================
  //  ESTILOS ESPECÍFICOS PARA SEARCH SCREEN
  // =============================================
  searchHeader: {
    backgroundColor: '#2196F3',
    padding: 15,
    borderTopLeftRadius: 12,
    borderTopRightRadius: 12,
  },
  searchHeaderText: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  visitanteInfoCard: {
    backgroundColor: '#e0f7fa',
    padding: 15,
    borderRadius: 10,
    marginHorizontal: 16,
    marginBottom: 15,
    borderLeftWidth: 4,
    borderLeftColor: '#00bcd4',
  },
  historicoCount: {
    backgroundColor: '#ffeb3b',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 10,
    marginLeft: 8,
  },
  historicoCountText: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#333',
  },

  // =============================================
  //  ANIMAÇÕES E TRANSITIONS
  // =============================================
  fadeIn: {
    opacity: 0,
  },
  fadeInActive: {
    opacity: 1,
  },
  slideUp: {
    transform: [{ translateY: 50 }],
  },
  slideUpActive: {
    transform: [{ translateY: 0 }],
  },

  // =============================================
  // ESTILOS ADICIONAIS PARA O REGISTER SCREEN
  // =============================================
  
  // Container para input com loading
  inputContainer: {
    position: 'relative',
  },
  
  // Indicador de loading dentro do input
  inputLoading: {
    position: 'absolute',
    right: 10,
    top: 12,
  },
  
  // Texto de instrução
  instructionText: {
    fontSize: 12,
    color: '#7f8c8d',
    fontStyle: 'italic',
    marginTop: 4,
    marginBottom: 8,
  },
  
  // Item de endereço no modal (melhorado)
  enderecoItem: {
    padding: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
    backgroundColor: '#f8f9fa',
    borderRadius: 8,
    marginVertical: 4,
  },
  enderecoContent: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  enderecoIcon: {
    fontSize: 16,
    marginRight: 8,
    marginTop: 2,
  },
  enderecoTextContainer: {
    flex: 1,
  },
  enderecoMainText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#2c3e50',
    marginBottom: 2,
  },
  enderecoSubText: {
    fontSize: 12,
    color: '#7f8c8d',
    marginBottom: 2,
  },
  enderecoDetailText: {
    fontSize: 10,
    color: '#95a5a6',
    fontStyle: 'italic',
  },
  
  // Badge para quantidade de resultados
  resultsBadge: {
    backgroundColor: '#3498db',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 10,
    marginLeft: 8,
  },
  resultsBadgeText: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
  
  // Mensagem de nenhum resultado
  noResultsText: {
    textAlign: 'center',
    color: '#95a5a6',
    fontSize: 14,
    fontStyle: 'italic',
    padding: 20,
  },
  
  // Container de debug (temporário)
  debugContainer: {
    backgroundColor: '#fff3cd',
    padding: 10,
    borderRadius: 5,
    marginVertical: 10,
    borderLeftWidth: 4,
    borderLeftColor: '#ffc107',
  },
  debugTitle: {
    fontSize: 12,
    fontFamily: 'monospace',
    fontWeight: 'bold',
    color: '#856404',
    marginBottom: 4,
  },
  debugContent: {
    fontSize: 10,
    fontFamily: 'monospace',
    color: '#856404',
  },
});