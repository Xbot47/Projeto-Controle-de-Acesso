import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
  FlatList,
  Modal
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import apiClient from '../api/apiClient';
import styles from '../styles/theme';

export default function RegisterScreen({ navigation }) {
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);
  const [loadingBusca, setLoadingBusca] = useState(false);
  const [loadingEnderecos, setLoadingEnderecos] = useState(false);
  const [loadingCategorias, setLoadingCategorias] = useState(true);

  // Estados principais
  const [categorias, setCategorias] = useState([]);
  const [enderecosSugeridos, setEnderecosSugeridos] = useState([]);
  const [visitanteEncontrado, setVisitanteEncontrado] = useState(null);
  const [showEnderecosModal, setShowEnderecosModal] = useState(false);
  const [showCategoriasModal, setShowCategoriasModal] = useState(false);

  // Dados do formulário
  const [formData, setFormData] = useState({
    documento: '',
    nome: '',
    sobrenome: '',
    categoria: '',
    codigoCategoria: null,
    numeroBusca: '',
    nomeSetorVisitado: '',
    nomeUnidadeVisitado: '',
    nomeVisitado: '',
    sobrenomeVisitado: '',
    observacao: ''
  });

  //Carrega categorias ao abrir
  useEffect(() => {
    carregarCategorias();
  }, []);

  //Busca visitante automaticamente pela placa
  useEffect(() => {
    const placaLimpa = formData.documento.replace(/[^A-Z0-9]/g, '');
    if (placaLimpa.length >= 6) {
      buscarVisitantePorPlaca(placaLimpa);
    } else {
      setVisitanteEncontrado(null);
      if (formData.documento.length < 6) {
        setFormData(prev => ({
          ...prev,
          nome: '',
          sobrenome: ''
        }));
      }
    }
  }, [formData.documento]);

  //Busca endereços
  useEffect(() => {
    const termo = formData.numeroBusca.trim();
    
    if (termo.length < 2) {
      limparCamposEndereco();
      return;
    }

    const timer = setTimeout(() => {
      buscarEnderecosEmostrarModal(termo);
    }, 600);

    return () => clearTimeout(timer);
  }, [formData.numeroBusca]);

  //Função auxiliar para limpar campos de endereço
  const limparCamposEndereco = () => {
    setEnderecosSugeridos([]);
    setShowEnderecosModal(false);
    setFormData(prev => ({
      ...prev,
      nomeSetorVisitado: '',
      nomeUnidadeVisitado: '',
      nomeVisitado: '',
      sobrenomeVisitado: '',
    }));
  };

  // Busca endereços e mostra modal
  const buscarEnderecosEmostrarModal = async (termo) => {
    if (!termo.trim() || termo.length < 2) {
      limparCamposEndereco();
      return;
    }
    
    try {
      setLoadingEnderecos(true);
      const response = await apiClient.buscarEnderecosPorNumero(termo);

      if (response.data?.success && response.data.enderecos && response.data.enderecos.length > 0) {
        const enderecos = response.data.enderecos;
        setEnderecosSugeridos(enderecos);
        setShowEnderecosModal(true);
      } else {
        Alert.alert(
          '📍 Nenhum endereço encontrado', 
          `Não encontramos endereços para "${termo}".\n\nVerifique o número ou digite outra busca.`,
          [{ text: 'OK' }]
        );
        limparCamposEndereco();
      }
    } catch (error) {
      console.log('❌ Erro na busca de endereços:', error);
      Alert.alert('Erro', 'Falha ao buscar endereços. Tente novamente.');
      limparCamposEndereco();
    } finally {
      setLoadingEnderecos(false);
    }
  };

  //Carrega categorias
  const carregarCategorias = async () => {
    try {
      setLoadingCategorias(true);
      const response = await apiClient.getCategorias();
      
      if (response.data && Array.isArray(response.data)) {
        setCategorias(response.data);

        const categoriaVisitante = response.data.find(cat =>
          cat.nome.toUpperCase().includes('VISITANTE')
        ) || response.data[0];

        if (categoriaVisitante) {
          updateField('categoria', categoriaVisitante.nome);
          updateField('codigoCategoria', categoriaVisitante.codigo);
        }
      } else {
        throw new Error('Resposta de categorias inválida');
      }
    } catch (error) {
      console.log('❌ Erro ao carregar categorias:', error);
      Alert.alert('Aviso', 'Erro ao carregar categorias. Use valores padrão.');
    } finally {
      setLoadingCategorias(false);
    }
  };

  // Busca visitante pela placa - BUSCA DIRETA NO HISTÓRICO
  const buscarVisitantePorPlaca = async (placa) => {
    if (!placa.trim() || placa.length < 6) return;
    
    try {
      setLoadingBusca(true);
      console.log(`🔍 Buscando histórico da placa: ${placa}`);
      
      const response = await apiClient.buscarPorPlaca(placa);

      if (response.data?.success && response.data.visitante) {
        const visitante = response.data.visitante;
        setVisitanteEncontrado(response.data);
        
        console.log('✅ Histórico encontrado:', response.data.totalRegistros, 'registros');
        console.log('🏷️ Categoria do último registro:', visitante.categoria);
        console.log('🔢 Código categoria:', visitante.codigoCategoria);
        
        // ENCONTRA A CATEGORIA NA LISTA
        let categoriaEncontrada = null;
        
        // 1. Tenta pelo código da categoria do histórico
        if (visitante.codigoCategoria) {
          categoriaEncontrada = categorias.find(cat => cat.codigo === visitante.codigoCategoria);
        }
        
        // 2. Tenta pelo nome da categoria do histórico
        if (!categoriaEncontrada && visitante.categoria) {
          categoriaEncontrada = categorias.find(cat => 
            cat.nome.toUpperCase() === visitante.categoria.toUpperCase()
          );
          
          // 3. Busca parcial se não encontrar exato
          if (!categoriaEncontrada) {
            categoriaEncontrada = categorias.find(cat => 
              visitante.categoria.toUpperCase().includes(cat.nome.toUpperCase()) ||
              cat.nome.toUpperCase().includes(visitante.categoria.toUpperCase())
            );
          }
        }
        
        // 4. Fallback para VISITANTE
        if (!categoriaEncontrada) {
          categoriaEncontrada = categorias.find(cat =>
            cat.nome.toUpperCase().includes('VISITANTE')
          ) || categorias[0];
        }
        
        console.log('✅ Categoria determinada:', categoriaEncontrada?.nome);
        
        // ATUALIZA O FORMULÁRIO COM OS DADOS DO ÚLTIMO HISTÓRICO
        setFormData(prev => ({
          ...prev,
          nome: visitante.nome || '',
          sobrenome: visitante.sobrenome || '',
          categoria: categoriaEncontrada?.nome || prev.categoria,
          codigoCategoria: categoriaEncontrada?.codigo || prev.codigoCategoria,
        }));
        
      } else {
        console.log('🆕 Nova placa - sem histórico');
        setVisitanteEncontrado(null);
        
        const categoriaPadrao = categorias.find(cat =>
          cat.nome.toUpperCase().includes('VISITANTE')
        ) || categorias[0];
        
        setFormData(prev => ({
          ...prev,
          nome: '',
          sobrenome: '',
          categoria: categoriaPadrao?.nome || prev.categoria,
          codigoCategoria: categoriaPadrao?.codigo || prev.codigoCategoria,
        }));
      }
    } catch (error) {
      console.log('❌ Erro na busca por placa:', error);
      setVisitanteEncontrado(null);
      setFormData(prev => ({
        ...prev,
        nome: '',
        sobrenome: ''
      }));
    } finally {
      setLoadingBusca(false);
    }
  };

  // Seleciona endereço
  const selecionarEndereco = (endereco) => {
    try {
      setFormData(prev => ({
        ...prev,
        nomeSetorVisitado: endereco.nomeSetorVisitado || endereco.setor || '',
        nomeUnidadeVisitado: endereco.nomeUnidadeVisitado || endereco.unidade || endereco.rua || '',
        nomeVisitado: endereco.nomeVisitado || endereco.proprietario || '',
        sobrenomeVisitado: endereco.sobrenomeVisitado || '',
      }));
      
      setShowEnderecosModal(false);
      setEnderecosSugeridos([]);
    } catch (e) {
      console.log('❌ Erro ao selecionar endereço:', e);
      setShowEnderecosModal(false);
    }
  };

  // Selecionar categoria
  const selecionarCategoria = (categoria) => {
    updateField('categoria', categoria.nome);
    updateField('codigoCategoria', categoria.codigo);
    setShowCategoriasModal(false);
  };

  // Envia o registro - FUNÇÃO COMPLETA
const handleRegister = async () => {
  console.log('🎯 ========== INICIANDO HANDLE REGISTER ==========');
  
  // VALIDAÇÕES
  if (!formData.documento.trim()) {
    return Alert.alert('Atenção', 'Placa é obrigatória');
  }
  if (!formData.nome.trim()) {
    return Alert.alert('Atenção', 'Nome é obrigatório');
  }
  if (!formData.nomeSetorVisitado.trim()) {
    return Alert.alert('Atenção', 'Número é obrigatório - selecione um endereço');
  }
  if (!formData.nomeUnidadeVisitado.trim()) {
    return Alert.alert('Atenção', 'Rua é obrigatória - selecione um endereço');
  }
  
  if (!formData.codigoCategoria) {
    return Alert.alert('Erro Crítico', 'Código da categoria inválido. Recarregue as categorias.');
  }

  const dataToSend = {
    documento: formData.documento.toUpperCase().replace(/[^A-Z0-9]/g, ''),
    nome: formData.nome.trim(),
    sobrenome: formData.sobrenome.trim(),
    codigoCategoria: formData.codigoCategoria,
    setor: formData.nomeSetorVisitado.trim(),
    unidade: formData.nomeUnidadeVisitado.trim(),
    proprietario: `${formData.nomeVisitado} ${formData.sobrenomeVisitado}`.trim(),
    categoria: formData.categoria,
    observacao: formData.observacao.trim(),
  };

  console.log('📤 Dados para envio:', dataToSend);

  try {
    setLoading(true);
    const response = await apiClient.registrarEntrada(dataToSend);
    
    if (response.data && response.data.success) {
      Alert.alert('✅ Sucesso!', `Entrada registrada para ${formData.nome}`);
      resetForm();
    } else {
      Alert.alert('Erro', response.data?.error || 'Falha ao registrar');
    }
  } catch (error) {
    console.log('💥 ERRO NO REGISTRO:', error);
    let errorMessage = error.message || 'Erro ao registrar entrada';
    
    if (error.response?.status === 401) {
      errorMessage = 'Token expirado. Faça login novamente.';
    } else if (error.response?.status === 400) {
      errorMessage = 'Dados inválidos. Verifique os campos.';
    } else if (error.response?.status === 500) {
      errorMessage = 'Erro interno do servidor. Tente novamente.';
    }
    
    Alert.alert('Erro', errorMessage);
  } finally {
    setLoading(false);
  }
};

  const resetForm = () => {
    const categoriaPadrao = categorias.find(cat => 
      cat.nome.toUpperCase().includes('VISITANTE')
    ) || categorias[0];
    
    setFormData({
      documento: '',
      nome: '',
      sobrenome: '',
      categoria: categoriaPadrao?.nome || '',
      codigoCategoria: categoriaPadrao?.codigo || null,
      numeroBusca: '',
      nomeSetorVisitado: '',
      nomeUnidadeVisitado: '',
      nomeVisitado: '',
      sobrenomeVisitado: '',
      observacao: ''
    });
    setVisitanteEncontrado(null);
    setEnderecosSugeridos([]);
    setShowEnderecosModal(false);
    setShowCategoriasModal(false);
  };

  const updateField = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  // Render item para endereços
  const renderEnderecoItem = ({ item, index }) => (
    <TouchableOpacity 
      style={[styles.suggestionItem, { marginVertical: 4 }]} 
      onPress={() => selecionarEndereco(item)}
    >
      <View style={{ flexDirection: 'row', alignItems: 'center' }}>
        <Text style={{ fontSize: 18, marginRight: 10 }}>🏠</Text>
        <View style={{ flex: 1 }}>
          <Text style={styles.suggestionText}>
            {item.nomeSetorVisitado} - {item.nomeUnidadeVisitado}
          </Text>
          {(item.nomeVisitado || item.proprietario) && (
            <Text style={styles.suggestionSubtext}>
              👤 {item.nomeVisitado || item.proprietario} {item.sobrenomeVisitado || ''}
            </Text>
          )}
        </View>
      </View>
    </TouchableOpacity>
  );

  // Render item para categorias
  const renderCategoriaItem = ({ item, index }) => (
    <TouchableOpacity 
      style={[
        styles.suggestionItem,
        { 
          marginVertical: 4, 
          paddingVertical: 12,
          backgroundColor: formData.codigoCategoria === item.codigo ? '#E8F5E8' : '#FFF'
        }
      ]} 
      onPress={() => selecionarCategoria(item)}
    >
      <View style={{ flexDirection: 'row', alignItems: 'center' }}>
        <Text style={{ fontSize: 18, marginRight: 12 }}>📋</Text>
        <View style={{ flex: 1 }}>
          <Text style={[
            styles.suggestionText,
            { fontWeight: formData.codigoCategoria === item.codigo ? 'bold' : 'normal' }
          ]}>
            {item.nome}
          </Text>
        </View>
        {formData.codigoCategoria === item.codigo && (
          <Text style={{ fontSize: 20, color: '#4CAF50', fontWeight: 'bold' }}>✓</Text>
        )}
      </View>
    </TouchableOpacity>
  );

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView contentContainerStyle={styles.scrollContainer}>
        <Text style={styles.title}>📝 Registrar Nova Entrada</Text>

        {/* PLACA */}
        <Text style={styles.label}>🚗 Placa do Veículo *</Text>
        <TextInput
          style={styles.input}
          value={formData.documento}
          onChangeText={(v) => updateField('documento', v.toUpperCase())}
          placeholder="Ex: ABC1234"
          autoCapitalize="characters"
          maxLength={7}
        />

        {/* FEEDBACK STATUS PLACA - APENAS UMA LINHA */}
        {loadingBusca && <Text style={styles.loadingText}>🔍 Buscando visitante...</Text>}
        {!loadingBusca && formData.documento.length >= 6 && visitanteEncontrado && (
          <View style={styles.foundCard}>
            <Text style={styles.successText}>✅ Visitante encontrado</Text>
          </View>
        )}
        {!loadingBusca && formData.documento.length >= 6 && !visitanteEncontrado && (
          <Text style={styles.infoText}>🆕 Nova placa - preencha manualmente</Text>
        )}

        {/* CATEGORIA - APENAS UMA SEÇÃO */}
        <Text style={styles.label}>📋 Categoria *</Text>
        
        {/* Botão para abrir modal de categorias */}
        <TouchableOpacity
          style={[styles.input, { 
            flexDirection: 'row', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            paddingVertical: 12,
            borderColor: formData.categoria ? '#4CAF50' : '#CCCCCC',
            borderWidth: formData.categoria ? 2 : 1
          }]}
          onPress={() => setShowCategoriasModal(true)}
          disabled={loadingCategorias}
        >
          <Text style={{ 
            color: formData.categoria ? '#000' : '#999',
            fontSize: 16,
            fontWeight: formData.categoria ? '600' : 'normal'
          }}>
            {formData.categoria || 'Selecione uma categoria...'}
          </Text>
          <Text style={{ fontSize: 18, color: '#007AFF' }}>▼</Text>
        </TouchableOpacity>

        {/* NOME */}
        <Text style={styles.label}>👤 Nome do Visitante *</Text>
        <TextInput
          style={styles.input}
          value={formData.nome}
          onChangeText={(v) => updateField('nome', v)}
          placeholder="Nome do visitante"
        />

        {/* SOBRENOME */}
        <Text style={styles.label}>👤 Sobrenome do Visitante</Text>
        <TextInput
          style={styles.input}
          value={formData.sobrenome}
          onChangeText={(v) => updateField('sobrenome', v)}
          placeholder="Sobrenome do visitante"
        />

        {/* CAMPO DE BUSCA DE ENDEREÇO */}
        <Text style={styles.label}>📍 Digite o Número ou Nome do Endereço *</Text>
        <View style={{ position: 'relative' }}>
          <TextInput
            style={styles.input}
            value={formData.numeroBusca}
            onChangeText={(v) => updateField('numeroBusca', v.toUpperCase())}
            placeholder="Ex: 177, CB, COSTA BRAVA"
            autoCapitalize="characters"
          />
          {loadingEnderecos && (
            <View style={{ position: 'absolute', right: 10, top: 12 }}>
              <ActivityIndicator size="small" color="#007AFF" />
            </View>
          )}
        </View>

        {/* CAMPOS AUTOMÁTICOS DO ENDEREÇO */}
        <Text style={styles.label}>📍 Número</Text>
        <TextInput 
          style={[styles.input, styles.readOnlyInput]} 
          value={formData.nomeSetorVisitado} 
          editable={false}
          placeholder="Selecione um endereço acima"
        />
        
        <Text style={styles.label}>🛣️ Rua</Text>
        <TextInput 
          style={[styles.input, styles.readOnlyInput]} 
          value={formData.nomeUnidadeVisitado} 
          editable={false}
          placeholder="Selecione um endereço acima"
        />
        
        <Text style={styles.label}>👥 Proprietário</Text>
        <TextInput 
          style={[styles.input, styles.readOnlyInput]} 
          value={formData.nomeVisitado} 
          editable={false}
          placeholder="Preenchido automaticamente"
        />

        {/* OBSERVAÇÃO */}
        <Text style={styles.label}>📝 Observação</Text>
        <TextInput
          style={[styles.input, { height: 60 }]}
          value={formData.observacao}
          onChangeText={(v) => updateField('observacao', v)}
          multiline
          placeholder="Observações adicionais"
        />

        {/* BOTÕES */}
        <TouchableOpacity
          style={[styles.button, loading && styles.buttonDisabled]}
          onPress={handleRegister}
          disabled={loading}
        >
          {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>✅ Registrar Entrada</Text>}
        </TouchableOpacity>

        <TouchableOpacity style={styles.secondaryButton} onPress={resetForm}>
          <Text style={styles.secondaryButtonText}>🗑️ Limpar Campos</Text>
        </TouchableOpacity>

        {/* MODAL DE SELEÇÃO DE CATEGORIAS */}
        <Modal 
          visible={showCategoriasModal} 
          animationType="slide" 
          transparent
          onRequestClose={() => setShowCategoriasModal(false)}
        >
          <View style={styles.modalOverlay}>
            <View style={[styles.modalContent, { maxHeight: '80%' }]}>
              <Text style={styles.modalTitle}>📋 Selecione a Categoria</Text>
              <Text style={styles.modalSubtitle}>
                {categorias.length} categoria(s) disponível(is)
              </Text>
              
              <FlatList
                data={categorias}
                renderItem={renderCategoriaItem}
                keyExtractor={(item, i) => i.toString()}
                style={styles.modalList}
                showsVerticalScrollIndicator={true}
              />
              
              <TouchableOpacity 
                style={styles.modalCloseButton} 
                onPress={() => setShowCategoriasModal(false)}
              >
                <Text style={styles.modalCloseButtonText}>Fechar</Text>
              </TouchableOpacity>
            </View>
          </View>
        </Modal>

        {/* MODAL DE SELEÇÃO DE ENDEREÇOS */}
        <Modal 
          visible={showEnderecosModal} 
          animationType="slide" 
          transparent
          onRequestClose={() => setShowEnderecosModal(false)}
        >
          <View style={styles.modalOverlay}>
            <View style={[styles.modalContent, { maxHeight: '80%' }]}>
              <Text style={styles.modalTitle}>📍 Selecione o Endereço</Text>
              <Text style={styles.modalSubtitle}>
                {enderecosSugeridos.length} endereço(s) encontrado(s)
              </Text>
              
              <FlatList
                data={enderecosSugeridos}
                renderItem={renderEnderecoItem}
                keyExtractor={(item, i) => i.toString()}
                style={styles.modalList}
                showsVerticalScrollIndicator={true}
              />
              
              <TouchableOpacity 
                style={styles.modalCloseButton} 
                onPress={() => setShowEnderecosModal(false)}
              >
                <Text style={styles.modalCloseButtonText}>Fechar</Text>
              </TouchableOpacity>
            </View>
          </View>
        </Modal>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}