import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Alert,
  ActivityIndicator,
  Platform,
  FlatList
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import apiClient from '../api/apiClient';
import styles from '../styles/theme';
import DateTimePicker from '@react-native-community/datetimepicker';

export default function AdvancedSearchScreen({ navigation }) {
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);
  const [resultados, setResultados] = useState([]);
  
  // Estados para busca por período
  const [dataInicio, setDataInicio] = useState(new Date());
  const [dataFim, setDataFim] = useState(new Date());
  const [horaInicio, setHoraInicio] = useState('00:00');
  const [horaFim, setHoraFim] = useState('23:59');
  const [showDataInicioPicker, setShowDataInicioPicker] = useState(false);
  const [showDataFimPicker, setShowDataFimPicker] = useState(false);

  // Formatar data para o padrão brasileiro
  const formatarData = (data) => {
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${dia}/${mes}/${ano}`;
  };

  // Formatar data para API (YYYY-MM-DD)
  const formatarDataParaAPI = (data) => {
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${ano}-${mes}-${dia}`;
  };

  //Formatar hora automaticamente
  const formatarHora = (text, campo) => {
    // Remove tudo que não é número
    const numeros = text.replace(/[^0-9]/g, '');
    
    // Limita a 4 dígitos
    let horaFormatada = numeros.slice(0, 4);
    
    // Adiciona os dois pontos automaticamente
    if (horaFormatada.length > 2) {
      horaFormatada = horaFormatada.slice(0, 2) + ':' + horaFormatada.slice(2);
    }
    
    // Validações básicas
    if (horaFormatada.length >= 3) {
      const horas = parseInt(horaFormatada.slice(0, 2));
      const minutos = parseInt(horaFormatada.slice(3, 5));
      
      // Corrige horas inválidas
      if (horas > 23) {
        horaFormatada = '23:' + (horaFormatada.slice(3) || '00');
      }
      
      // Corrige minutos inválidos
      if (minutos > 59) {
        horaFormatada = horaFormatada.slice(0, 3) + '59';
      }
    }
    
    // Atualiza o estado correto
    if (campo === 'inicio') {
      setHoraInicio(horaFormatada);
    } else {
      setHoraFim(horaFormatada);
    }
  };

  // Manipular seleção de data início
  const onDataInicioChange = (event, selectedDate) => {
    setShowDataInicioPicker(Platform.OS === 'ios');
    if (selectedDate) {
      setDataInicio(selectedDate);
    }
  };

  // Manipular seleção de data fim
  const onDataFimChange = (event, selectedDate) => {
    setShowDataFimPicker(Platform.OS === 'ios');
    if (selectedDate) {
      setDataFim(selectedDate);
    }
  };

  //Ordenar resultados por data (mais ANTIGO primeiro)
  const ordenarResultados = (resultados) => {
    return [...resultados].sort((a, b) => {
      const dataA = a.historico?.dataHoraEntrada ? new Date(a.historico.dataHoraEntrada) : new Date(0);
      const dataB = b.historico?.dataHoraEntrada ? new Date(b.historico.dataHoraEntrada) : new Date(0);
      return dataA - dataB; //ORDEM CRESCENTE (mais ANTIGO primeiro)
    });
  };

  // Buscar por período
  const buscarPorPeriodo = async () => {
    if (!isAuthenticated) {
      Alert.alert('Erro', 'Sistema não autenticado');
      return;
    }

    //VALIDAÇÃO DAS HORAS
    if (!validarHora(horaInicio) || !validarHora(horaFim)) {
      Alert.alert('Erro', 'Por favor, digite horas válidas no formato HH:MM');
      return;
    }

    try {
      setLoading(true);
      setResultados([]);

      const dataInicioFormatada = formatarDataParaAPI(dataInicio);
      const dataFimFormatada = formatarDataParaAPI(dataFim);

      console.log(`📅 Buscando históricos de ${dataInicioFormatada} ${horaInicio} até ${dataFimFormatada} ${horaFim}`);

      const response = await apiClient.buscarHistoricosPorPeriodo(
        dataInicioFormatada,
        dataFimFormatada,
        horaInicio,
        horaFim
      );

      if (response.data.success) {
        //CORREÇÃO: Ordenar do mais ANTIGO para o mais RECENTE
        const resultadosOrdenados = ordenarResultados(response.data.historicos || []);
        setResultados(resultadosOrdenados);
        console.log(`✅ ${resultadosOrdenados.length} registros ordenados do mais antigo para o mais recente`);
        Alert.alert('Sucesso', `${response.data.totalRegistros} registros encontrados`);
      } else {
        Alert.alert('Aviso', response.data.error || 'Nenhum resultado encontrado');
      }

    } catch (error) {
      console.log('❌ Erro na busca por período:', error);
      Alert.alert('Erro', error.message || 'Erro na busca');
    } finally {
      setLoading(false);
    }
  };

  // Validar formato de hora
  const validarHora = (hora) => {
    const regex = /^([0-1][0-9]|2[0-3]):[0-5][0-9]$/;
    return regex.test(hora);
  };

  //useCallback para otimizar performance
  const renderItemHistorico = useCallback(({ item, index }) => {
    const historico = item.historico;
    const visitante = item.visitante;

    //Formatar data para debug
    const dataHora = historico?.dataHoraEntrada ? new Date(historico.dataHoraEntrada) : null;
    const dataFormatada = dataHora ? dataHora.toLocaleDateString('pt-BR') : 'N/A';
    const horaFormatada = dataHora ? dataHora.toLocaleTimeString('pt-BR') : 'N/A';

    const categoria = historico?.nomeCategoriaVisitante || 
    item.categoria || 
    visitante?.caregoria || 'N/A';

    
    return (
      <View style={styles.card}>
        <Text style={styles.cardTitle}>
          🗓️ {dataFormatada} - {horaFormatada}
        </Text>
        
        <Text style={styles.cardText}>
          <Text style={{fontWeight: 'bold'}}>🚗 Placa: </Text>
          {historico?.documentoVisitante || 'N/A'}
        </Text>
        
        <Text style={styles.cardText}>
          <Text style={{fontWeight: 'bold'}}>👤 Visitante: </Text>
          {visitante?.nome || 'N/A'}
        </Text>
        
        <Text style={styles.cardText}>
          <Text style={{fontWeight: 'bold'}}>🏷️ Categoria: </Text>
          {categoria}
        </Text>

        <Text style={styles.cardText}>
          <Text style={{fontWeight: 'bold'}}>📍 Local: </Text>
          {item.localCompleto || 'N/A'}
        </Text>
        
        <Text style={styles.cardText}>
          <Text style={{fontWeight: 'bold'}}>👥 Pessoa Visitada: </Text>
          {item.pessoaVisitada || 'N/A'}
        </Text>
      </View>
    );
  }, []);

  //Key extractor otimizada
  const keyExtractor = useCallback((item, index) => {
    return item.historico?.codigo?.toString() || `hist-${index}`;
  }, []);

  // Componente de seção de busca
  const SecaoBusca = ({ titulo, children }) => (
    <View style={styles.form}>
      <Text style={styles.sectionTitle}>{titulo}</Text>
      {children}
    </View>
  );

  return (
    <View style={styles.container}>
      {/*FORMULÁRIO FIXO NO TOPO */}
      <View style={{ flex: resultados.length > 0 ? 0.35 : 1 }}>
        <ScrollView 
          contentContainerStyle={styles.scrollContainer}
          showsVerticalScrollIndicator={true}
        >
          <Text style={styles.title}>🔍 Busca por Período</Text>
          <SecaoBusca titulo="Selecione o Período">
            <Text style={styles.label}>Data Início</Text>
            <TouchableOpacity 
              style={[styles.input, { justifyContent: 'center' }]}
              onPress={() => setShowDataInicioPicker(true)}
            >
              <Text style={{ fontSize: 16 }}>📅 {formatarData(dataInicio)}</Text>
            </TouchableOpacity>

            <Text style={styles.label}>Data Fim</Text>
            <TouchableOpacity 
              style={[styles.input, { justifyContent: 'center' }]}
              onPress={() => setShowDataFimPicker(true)}
            >
              <Text style={{ fontSize: 16 }}>📅 {formatarData(dataFim)}</Text>
            </TouchableOpacity>
            <View style={styles.row}>
              <View style={styles.flex1}>
                <Text style={styles.label}>Hora Início</Text>
                <TextInput
                  style={styles.input}
                  value={horaInicio}
                  onChangeText={(text) => formatarHora(text, 'inicio')}
                  placeholder="00:00"
                  keyboardType="numeric"
                  maxLength={5}
                  returnKeyType="done"
                  blurOnSubmit={true}
                />
              </View>
              <View style={styles.flex1}>
                <Text style={styles.label}>Hora Fim</Text>
                <TextInput
                  style={styles.input}
                  value={horaFim}
                  onChangeText={(text) => formatarHora(text, 'fim')}
                  placeholder="23:59"
                  keyboardType="numeric"
                  maxLength={5}
                  returnKeyType="done"
                  blurOnSubmit={true}
                />
              </View>
            </View>
            
            {/*TEXTO DE AJUDA PARA AS HORAS */}
            <Text style={[styles.infoText, { fontSize: 12, marginTop: -10, marginBottom: 10 }]}>
              💡 Digite as horas no formato 24h (ex: 08:30, 14:15, 23:59)
            </Text>
            
            {showDataInicioPicker && (
              <DateTimePicker
                value={dataInicio}
                mode="date"
                display={Platform.OS === 'ios' ? 'spinner' : 'default'}
                onChange={onDataInicioChange}
              />
            )}
            {showDataFimPicker && (
              <DateTimePicker
                value={dataFim}
                mode="date"
                display={Platform.OS === 'ios' ? 'spinner' : 'default'}
                onChange={onDataFimChange}
                />
            )}
          </SecaoBusca>
          <TouchableOpacity
            style={[styles.button, loading && styles.buttonDisabled]}
            onPress={buscarPorPeriodo}
            disabled={loading}>
            {loading ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={styles.buttonText}>📅 Buscar por Período</Text>
            )}
          </TouchableOpacity>
          {resultados.length === 0 && !loading && (
            <View style={styles.infoBox}>
              <Text style={styles.statusTitle}>💡 Como Usar</Text>
              <Text style={styles.statusDetail}>
                • Selecione as datas de início e fim do período
              </Text>
              <Text style={styles.statusDetail}>
                • Defina os horários de início e fim (formato 24h)
              </Text>
              <Text style={styles.statusDetail}>
                • Toque em "Buscar por Período" para ver os resultados
              </Text>
            </View>
          )}
        </ScrollView>
      </View>
      {resultados.length > 0 && (
        <View style={{ flex: 0.65, borderTopWidth: 0, borderTopColor: '#4CAF50' }}>
          <View style={[styles.form, { 
            marginBottom: 0, 
            borderBottomWidth: 0,
            paddingVertical: 12,
            backgroundColor: '#4CAF50'
          }]}>
            <Text style={[styles.sectionTitle, { color: "white", marginBottom: 8 }]}>
              📋 Resultados ({resultados.length})
            </Text>
            <TouchableOpacity
              style={[styles.secondaryButton, { marginBottom: 5, backgroundColor: "white" }]}
              onPress={() => setResultados([])}>
              <Text style={[styles.secondaryButtonText, { color: "red" }]}>🗑️ Limpar Resultados</Text>
            </TouchableOpacity>
          </View>
          <FlatList
            data={resultados}
            renderItem={renderItemHistorico}
            keyExtractor={keyExtractor}
            style={{ flex: 1 }}
            contentContainerStyle={{ padding: 10 }}
            showsVerticalScrollIndicator={true}
            initialNumToRender={15}
            maxToRenderPerBatch={20}
            windowSize={10}
            removeClippedSubviews={true}
            inverted={false}
          />
        </View>
      )}
    </View>
  );
}