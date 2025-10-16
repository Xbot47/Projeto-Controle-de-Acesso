import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  FlatList,
  Alert,
  ActivityIndicator,
  Modal,
  Keyboard
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import apiClient from '../api/apiClient';
import styles from '../styles/theme';

export default function SearchScreen() {
  const { isAuthenticated } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingSugestoes, setLoadingSugestoes] = useState(false);
  const [historicos, setHistoricos] = useState([]);
  const [visitante, setVisitante] = useState(null);
  const [categoriaVisitante, setCategoriaVisitante] = useState('');
  
  // STATES PARA BUSCA INTELIGENTE
  const [sugestoes, setSugestoes] = useState([]);
  const [modalVisivel, setModalVisivel] = useState(false);
  const [buscaRealizada, setBuscaRealizada] = useState(false);
  
  // REFS PARA CONTROLE - CORREÇÃO CRÍTICA
  const bloqueiaBuscaAutomatica = useRef(false);
  const ultimoTermoBuscado = useRef('');

  // BUSCA INTELIGENTE - CHAMADA AO DIGITAR (COM DEBOUNCE)
  useEffect(() => {
    // Se está bloqueado (após seleção), não faz busca automática
    if (bloqueiaBuscaAutomatica.current) {
      return;
    }

    const timer = setTimeout(() => {
      const normalizedText = searchTerm.trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
      
      // Só busca se tiver pelo menos 2 caracteres E for diferente da última busca
      if (normalizedText.length >= 2 && normalizedText !== ultimoTermoBuscado.current) {
        buscarSugestoes(normalizedText);
      } else if (normalizedText.length < 2) {
        setSugestoes([]);
        setModalVisivel(false);
        ultimoTermoBuscado.current = '';
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [searchTerm]);

  const buscarSugestoes = async (termo) => {
    try {
      setLoadingSugestoes(true);
      ultimoTermoBuscado.current = termo;
      console.log(`🔍 Buscando sugestões para: ${termo}`);
      
      const response = await apiClient.buscaInteligente(termo);
      
      if (response.data.success && response.data.resultados) {
        const resultados = response.data.resultados;
        setSugestoes(resultados);
        
        // MOSTRA MODAL AUTOMATICAMENTE SE HOUVER SUGESTÕES
        if (resultados.length > 0) {
          setModalVisivel(true);
          console.log(`✅ ${resultados.length} sugestões encontradas`);
        } else {
          setModalVisivel(false);
          console.log('ℹ️ Nenhuma sugestão encontrada');
        }
      } else {
        setSugestoes([]);
        setModalVisivel(false);
      }
    } catch (error) {
      console.log('❌ Erro na busca inteligente:', error);
      setSugestoes([]);
      setModalVisivel(false);
    } finally {
      setLoadingSugestoes(false);
    }
  };

  // BUSCA DETALHADA QUANDO SELECIONA UMA PLACA OU CLICA EM BUSCAR
  const handleSearch = async (placaEspecifica = null) => {
    const termoBusca = placaEspecifica || searchTerm;
    
    if (!termoBusca.trim()) {
      Alert.alert('Atenção', 'Digite a placa ou documento para buscar');
      return;
    }

    if (!isAuthenticated) {
      Alert.alert('Erro', 'O sistema não está autenticado.');
      return;
    }
    
    const normalizedTerm = termoBusca.trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
    
    try {
      // BLOQUEIA BUSCA AUTOMÁTICA ENQUANTO FAZ A BUSCA PRINCIPAL
      bloqueiaBuscaAutomatica.current = true;
      
      setLoading(true);
      setVisitante(null);
      setHistoricos([]);
      setCategoriaVisitante('');
      
      // CORREÇÃO CRÍTICA: FECHA COMPLETAMENTE O MODAL
      setModalVisivel(false);
      setSugestoes([]);
      
      setBuscaRealizada(true);
      Keyboard.dismiss();

      console.log(`📞 Buscando dados completos para: ${normalizedTerm}`);
      
      // BUSCA EM PARALELO: Histórico E dados do visitante
      const [historicoResponse, visitanteResponse] = await Promise.all([
        apiClient.buscarHistoricoCompleto(normalizedTerm),
        apiClient.buscarPorPlaca(normalizedTerm) // Busca dados completos do visitante
      ]);

      if (historicoResponse.data.success) {
        const historicosData = historicoResponse.data.historicos || [];
        const primeiraCategoria = historicosData.length > 0 
          ? historicosData[0].historico?.nomeCategoriaVisitante || 'N/A'
          : 'N/A';

        // DADOS COMPLETOS DO VISITANTE (incluindo empresa)
        let empresa = 'Não informada';
        let nomeVisitante = historicoResponse.data.nome;
        
        if (visitanteResponse.data.success && visitanteResponse.data.visitante) {
          empresa = visitanteResponse.data.visitante.empresa || 'Não informada';
          nomeVisitante = visitanteResponse.data.visitante.nome || nomeVisitante;
        }

        setVisitante({
          documento: historicoResponse.data.documento,
          nome: nomeVisitante,
          empresa: empresa,
          totalVisitas: historicoResponse.data.totalVisitas,
        });
        
        setHistoricos(historicosData);
        setCategoriaVisitante(primeiraCategoria);
        
        console.log(`🎯 ${historicosData.length} entradas encontradas`);
        console.log(`🏢 Empresa: ${empresa}`);
        
      } else {
        Alert.alert('Aviso', historicoResponse.data.error || 'Nenhum histórico encontrado para esta placa');
      }

    } catch (error) {
      console.log('❌ Erro na busca:', error);
      Alert.alert('Erro', error.response?.data?.error || 'Erro na busca');
    } finally {
      setLoading(false);
      
      // LIBERA BUSCA AUTOMÁTICA APÓS 1 SEGUNDO (tempo suficiente para evitar reativação)
      setTimeout(() => {
        bloqueiaBuscaAutomatica.current = false;
        console.log('🔄 Busca automática liberada novamente');
      }, 1000);
    }
  };

  // SELECIONAR SUGESTÃO DA LISTA
  const selecionarSugestao = (sugestao) => {
    console.log(`🎯 Selecionado: ${sugestao.documento} - ${sugestao.nome}`);
    
    // BLOQUEIA IMEDIATAMENTE QUALQUER BUSCA AUTOMÁTICA
    bloqueiaBuscaAutomatica.current = true;
    
    // FECHA O MODAL E LIMPA TUDO
    setModalVisivel(false);
    setSugestoes([]);
    
    // ATUALIZA O CAMPO DE BUSCA
    setSearchTerm(sugestao.documento);
    
    // FAZ A BUSCA IMEDIATAMENTE
    handleSearch(sugestao.documento);
  };

  // FECHAR MODAL COMPLETAMENTE
  const fecharModalCompletamente = () => {
    setModalVisivel(false);
    setSugestoes([]);
    ultimoTermoBuscado.current = '';
  };

  // LIMPAR BUSCA
  const limparBusca = () => {
    setSearchTerm('');
    setVisitante(null);
    setHistoricos([]);
    setSugestoes([]);
    setModalVisivel(false);
    setBuscaRealizada(false);
    bloqueiaBuscaAutomatica.current = false;
    ultimoTermoBuscado.current = '';
  };

  // RENDERIZAR SUGESTÕES NO MODAL
  const renderSugestao = ({ item, index }) => (
    <TouchableOpacity
      style={[
        styles.sugestaoItem,
        index % 2 === 0 ? styles.sugestaoItemPar : styles.sugestaoItemImpar
      ]}
      onPress={() => selecionarSugestao(item)}
    >
      <View style={styles.sugestaoContent}>
        <Text style={styles.sugestaoPlaca}>🚗 {item.documento}</Text>
        <Text style={styles.sugestaoNome}>👤 {item.nome}</Text>
        {item.sobrenome && (
          <Text style={styles.sugestaoSobrenome}>{item.sobrenome}</Text>
        )}
        {item.empresa && (
          <Text style={styles.sugestaoEmpresa}>🏢 {item.empresa}</Text>
        )}
        <Text style={styles.sugestaoVisitas}>
          📊 {item.numeroVisitas || 'N/A'} visitas
        </Text>
      </View>
    </TouchableOpacity>
  );

  //RENDERIZAR ITEM DO HISTÓRICO
  const renderHistoricoItem = ({ item, index }) => {
    const dataHora = item.historico?.dataHoraEntrada ? new Date(item.historico.dataHoraEntrada) : null;
    
    const destino = item.localCompleto || 
                   (item.destino ? `${item.destino.nomeSetorVisitado} / ${item.destino.nomeUnidadeVisitado}` : 
                   'Destino Não Especificado');
    
    const proprietario = item.pessoaVisitada || 
                        (item.destino ? `${item.destino.nomeVisitado} ${item.destino.sobrenomeVisitado || ''}`.trim() :
                        'Sistema');
    
    const categoria = item.historico?.nomeCategoriaVisitante || 'N/A';

    return (
      <View style={styles.card}>
        <Text style={styles.cardTitle}>
          🗓️ Entrada: {dataHora ? dataHora.toLocaleDateString('pt-BR') : 'N/A'}
        </Text>
        <Text style={styles.cardText}>
          <Text style={{fontWeight: 'bold'}}>⏰ Hora: </Text>
          {dataHora ? dataHora.toLocaleTimeString('pt-BR') : 'N/A'}
        </Text>
        
        <Text style={styles.cardText}> 
          <Text style={{fontWeight: 'bold'}}>📍 Destino: </Text>
          {destino}
        </Text>
        
        <Text style={styles.cardText}>
          <Text style={{fontWeight: 'bold'}}>👤 Pessoa Visitada: </Text>
          {proprietario}
        </Text>
        
        <Text style={styles.cardText}>
          <Text style={{fontWeight: 'bold'}}>🏷️ Categoria: </Text>
          {categoria}
        </Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>🔍 Buscar Histórico de Placa</Text>

      <View style={styles.form}>
        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
          <TextInput
            style={[styles.input, { flex: 1 }]}
            value={searchTerm}
            onChangeText={(text) => setSearchTerm(text.toUpperCase())}
            placeholder="Digite a placa (Ex: ABC123, ABC, 123, 5H60...)"
            autoCapitalize="characters"
            returnKeyType="search"
            onSubmitEditing={() => handleSearch()}
          />
          
          {/* BOTÃO LIMPAR - OPICIONAL */}
          {searchTerm.length > 0 && (
            <TouchableOpacity
              style={{ marginLeft: 10, padding: 10 }}
              onPress={limparBusca}
            >
              <Text style={{ color: '#ff6b6b', fontWeight: 'bold' }}>✕</Text>
            </TouchableOpacity>
          )}
        </View>

        {/*INDICADOR DE CARREGAMENTO DAS SUGESTÕES */}
        {loadingSugestoes && (
          <Text style={styles.loadingText}>🔍 Buscando sugestões...</Text>
        )}

        <TouchableOpacity
          style={[styles.button, (loading || !searchTerm.trim()) && styles.buttonDisabled, {backgroundColor: '#0072feff'}]}
          onPress={() => handleSearch()}
          disabled={loading || !searchTerm.trim() || !isAuthenticated}
        >
          {loading ? (
            <ActivityIndicator color="#ff6a00ff" />
          ) : (
            <Text style={styles.buttonText}>🚗 BUSCAR PLACA</Text>
          )}
        </TouchableOpacity>
      </View>

      {/*MODAL DE SUGESTÕES - CORRIGIDO */}
      <Modal
        visible={modalVisivel}
        animationType="slide"
        transparent={true}
        onRequestClose={fecharModalCompletamente}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>
              🎯 {sugestoes.length} placas encontradas para "{searchTerm}"
            </Text>
            
            <Text style={styles.modalSubtitle}>
              Toque em uma placa para ver o histórico completo
            </Text>
            
            <FlatList
              data={sugestoes}
              renderItem={renderSugestao}
              keyExtractor={(item) => item.documento}
              style={styles.sugestoesList}
              showsVerticalScrollIndicator={true}
              initialNumToRender={10}
            />
            
            <TouchableOpacity
              style={styles.modalFechar}
              onPress={fecharModalCompletamente}
            >
              <Text style={styles.modalFecharTexto}>❌ Fechar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
      
      {/*SEÇÃO DO VISITANTE COM EMPRESA */}
      {visitante && (
        <View style={[styles.form, {padding: 15, backgroundColor: '#e0f7fa', borderRadius: 10}]}>
          <Text style={styles.cardTitle}>👤 Visitante: {visitante.nome}</Text>
          
          <Text style={styles.cardText}>
            <Text style={{fontWeight: 'bold'}}>🚗 Placa/Doc: </Text> 
            <Text style={{fontWeight: 'bold', color: '#192463ff'}}>{visitante.documento}</Text>
          </Text>
          
          {/*EMPRESA DO VISITANTE */}
          <Text style={styles.cardText}>
            <Text style={{fontWeight: 'bold'}}>🏢 Empresa: </Text> 
            <Text style={{fontWeight: 'bold', color: '#192463ff'}}>{visitante.empresa}</Text>
          </Text>
          
          <Text style={styles.cardText}>
            <Text style={{fontWeight: 'bold'}}>📊 Total de visitas: </Text> 
            <Text style={{fontWeight: 'bold', color: '#192463ff'}}>{historicos.length}</Text>
          </Text>
          
          <Text style={styles.cardText}>
            <Text style={{fontWeight: 'bold'}}>🏷️ Categoria: </Text> 
            <Text style={{fontWeight: 'bold', color: '#192463ff'}}>{categoriaVisitante}</Text>
          </Text> 
        </View>
      )}

      {/*LISTA DE HISTÓRICOS */}
      <FlatList
        data={historicos}
        renderItem={renderHistoricoItem}
        keyExtractor={(item, index) => 
          item.historico?.codigo ? item.historico.codigo.toString() : `hist-${index}`
        }
        style={styles.resultsList}
        ListEmptyComponent={
          loading ? (
            <View style={styles.loadingContainer}>
              <ActivityIndicator size="large" color="#ff5100ff" />
              <Text style={styles.loadingText}>Buscando histórico...</Text>
            </View>
          ) : (
            <Text style={styles.emptyText}>
              {buscaRealizada 
                ? '📭 Nenhum histórico encontrado.' 
                : '🔍 Digite uma placa para buscar. Use 2+ caracteres para busca inteligente.'
              }
            </Text>
          )
        }
      />
    </View>
  );
}