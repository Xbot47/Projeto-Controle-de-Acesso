import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

const DEFAULT_API_URL = '/api/editpro';

class ApiClient {
  constructor() {
    this.currentBaseURL = DEFAULT_API_URL;
    this.token = null;
    this.isAuthenticated = false;

    this.initializeClient();
    this.loadInitialToken();
  }

  async loadInitialToken() {
    try {
      const storedToken = await AsyncStorage.getItem('auth_token');
      if (storedToken) {
        this.token = storedToken;
        this.isAuthenticated = true;
        console.log('🔑 Token carregado do storage');
      }
    } catch (error) {
      console.log('❌ Erro ao carregar token:', error);
    }
  }

  initializeClient() {
    this.client = axios.create({
      baseURL: this.currentBaseURL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      }
    });

    this.setupInterceptors();
  }

  setupInterceptors() {
    this.client.interceptors.request.use(
      (config) => {
        if (this.token) {
          config.headers.Authorization = `Bearer ${this.token}`;
        }
        console.log('🚀 REQUEST:', config.method?.toUpperCase(), config.url);
        return config;
      },
      (error) => {
        console.log('💥 REQUEST ERROR:', error);
        return Promise.reject(error);
      }
    );

    this.client.interceptors.response.use(
      (response) => {
        console.log('✅ RESPONSE:', response.status);
        return response;
      },
      (error) => {
        console.log('❌ API ERROR:', {
          message: error.message,
          status: error.response?.status,
          data: error.response?.data,
          url: error.config?.url
        });
        
        if (error.response?.status === 401) {
          this.token = null;
          this.isAuthenticated = false;
          AsyncStorage.removeItem('auth_token');
          error.message = 'Token inválido/expirado. Reautenticação necessária.';
        }
        return Promise.reject(error);
      }
    );
  }

  async getCategorias() {
    try {
      console.log('🔍 Buscando categorias do banco...');
      const response = await this.client.get('/categorias');
      
      // ✅ VERIFICA SE AS CATEGORIAS SÃO VÁLIDAS
      if (response.data && Array.isArray(response.data)) {
        console.log('✅ Categorias carregadas do banco:', response.data);
        return response;
      } else {
        throw new Error('Resposta inválida de categorias');
      }
    } catch (error) {
      console.log('❌ Erro ao buscar categorias do banco:', error);
      
      //FALLBACK: Tenta buscar endpoint alternativo para debug
      try {
        console.log('🔄 Tentando endpoint de debug...');
        const debugResponse = await this.client.get('/debug/categorias');
        if (debugResponse.data) {
          console.log('✅ Categorias do debug:', debugResponse.data);
          return debugResponse;
        }
      } catch (debugError) {
        console.log('❌ Debug também falhou:', debugError);
      }
      
      //FALLBACK FINAL: categorias padrão com códigos prováveis
      console.log('🔄 Usando fallback de categorias...');
      return {
        data: [
          { codigo: 10, nome: 'ENTREGA' },
          { codigo: 16, nome: 'VISITANTE' },
          { codigo: 17, nome: 'FUNCIONÁRIO(A)' },
          { codigo: 15, nome: 'MORADOR(A)' },
          { codigo: 6, nome: 'IFOOD/ZÉ DELIVERY/RAPPI' },
          { codigo: 5, nome: 'MOTO TRANS. PASSAGEIRO' },
          { codigo: 12, nome: 'SERVIÇO/OBRA' },
          { codigo: 3, nome: 'TÁXI' },
          { codigo: 18, nome: 'UBER/99/INDRIVE' }
        ]
      };
    }
  }

  //BUSCA ENDEREÇOS POR NÚMERO - CORRIGIDO
  async buscarEnderecosPorNumero(numero) {
  try {
    console.log('🎯 ========== API CLIENT: BUSCA ENDEREÇOS ==========');
    console.log('🔍 Número recebido:', numero);
    
    //LIMPA E PREPARA O NÚMERO
    const numeroLimpo = numero.toString().trim().toUpperCase();
    console.log('🔍 Número limpo:', numeroLimpo);
    
    //TENTA DIFERENTES ENDPOINTS
    const endpoints = [
      `/historicos-visitados/por-numero?numero=${encodeURIComponent(numeroLimpo)}`,
      `/historicos-visitados/busca?termo=${encodeURIComponent(numeroLimpo)}`,
      `/historicos?search=${encodeURIComponent(numeroLimpo)}`,
      `/enderecos?numero=${encodeURIComponent(numeroLimpo)}`
    ];
    
    let ultimoErro = null;
    
    for (const endpoint of endpoints) {
      try {
        console.log(`🔄 Tentando endpoint: ${endpoint}`);
        const response = await this.client.get(endpoint);
        console.log(`✅ Resposta de ${endpoint}:`, response.data);
        
        //VERIFICA DIFERENTES FORMATOS DE RESPOSTA
        if (response.data) {
          let enderecos = [];
          
          // Formato 1: { success: true, enderecos: [...] }
          if (response.data.success && Array.isArray(response.data.enderecos)) {
            enderecos = response.data.enderecos;
          }
          // Formato 2: { enderecos: [...] }
          else if (Array.isArray(response.data.enderecos)) {
            enderecos = response.data.enderecos;
          }
          // Formato 3: Array direto
          else if (Array.isArray(response.data)) {
            enderecos = response.data;
          }
          // Formato 4: { historicos: [...] } - extrai endereços
          else if (response.data.historicos && Array.isArray(response.data.historicos)) {
            enderecos = this.extrairEnderecosDeHistoricos(response.data.historicos);
          }
          // Formato 5: Objeto único
          else if (response.data.nomeSetorVisitado || response.data.setor) {
            enderecos = [response.data];
          }
          
          if (enderecos.length > 0) {
            console.log(`✅ ${enderecos.length} endereço(s) encontrado(s) no endpoint: ${endpoint}`);
            
            // ✅ NORMALIZA OS DADOS DOS ENDEREÇOS
            const enderecosNormalizados = enderecos.map(end => ({
              nomeSetorVisitado: end.nomeSetorVisitado || end.setor || end.numero || end.numeroBusca || numeroLimpo,
              nomeUnidadeVisitado: end.nomeUnidadeVisitado || end.unidade || end.rua || end.logradouro || '',
              nomeVisitado: end.nomeVisitado || end.proprietario || end.nome || '',
              sobrenomeVisitado: end.sobrenomeVisitado || end.sobrenome || '',
              localCompleto: end.localCompleto || `${end.nomeSetorVisitado} / ${end.nomeUnidadeVisitado}`,
              pessoaVisitada: end.pessoaVisitada || `${end.nomeVisitado} ${end.sobrenomeVisitado || ''}`.trim()
            }));
            
            return {
              data: {
                success: true,
                enderecos: enderecosNormalizados,
                origem: endpoint
              }
            };
          }
        }
      } catch (endpointError) {
        console.log(`❌ Endpoint ${endpoint} falhou:`, endpointError.message);
        ultimoErro = endpointError;
        continue; // Tenta o próximo endpoint
      }
    }
    
    //SE TODOS OS ENDPOINTS FALHAREM
    console.log('❌ Todos os endpoints falharam');
    return {
      data: {
        success: false,
        enderecos: [],
        message: 'Nenhum endereço encontrado em nenhum endpoint'
      }
    };
    
  } catch (error) {
    console.log('💥 ERRO GERAL NA BUSCA DE ENDEREÇOS:', error);
    return {
      data: {
        success: false,
        enderecos: [],
        message: `Erro na busca: ${error.message}`
      }
    };
  }
}

  //MÉTODO AUXILIAR PARA EXTRAIR ENDEREÇOS DE HISTÓRICOS - CORRIGIDO
  extrairEnderecosDeHistoricos(historicos) {
    const enderecosMap = new Map();
    
    historicos.forEach(historico => {
      if (historico.destino) {
        const key = `${historico.destino.nomeSetorVisitado}_${historico.destino.nomeUnidadeVisitado}`;
        if (!enderecosMap.has(key)) {
          enderecosMap.set(key, {
            nomeSetorVisitado: historico.destino.nomeSetorVisitado,
            nomeUnidadeVisitado: historico.destino.nomeUnidadeVisitado,
            nomeVisitado: historico.destino.nomeVisitado,
            sobrenomeVisitado: historico.destino.sobrenomeVisitado,
            localCompleto: historico.destino.localCompleto,
            pessoaVisitada: historico.destino.pessoaVisitada
          });
        }
      }
    });
    
    return Array.from(enderecosMap.values());
  }

  //BUSCAR VISITANTE POR PLACA
  async buscarPorPlaca(placa) {
  try {
    console.log(`🔍 Buscando histórico da placa: ${placa}`);
    
    // Busca direto no histórico com limite menor para performance
    const response = await this.client.get(`/visitantes/${encodeURIComponent(placa)}/historico-completo?limit=10&order=desc`);
    
    console.log('📦 Resposta completa do histórico:', response.data);

    if (response.data && response.data.historicos && response.data.historicos.length > 0) {
      console.log(`✅ ${response.data.historicos.length} registros encontrados no histórico`);
      
      // Pega o PRIMEIRO registro (mais recente) - já ordenado por data decrescente
      const ultimoRegistro = response.data.historicos[0];
      const historico = ultimoRegistro.historico;
      
      console.log('📅 Último registro encontrado:', {
        data: historico?.dataHoraEntrada,
        nome: historico?.nomeVisitante,
        categoria: historico?.nomeCategoriaVisitante,
        codigoCategoria: historico?.codigoCategoriaVisitante
      });
      
      // Cria visitante com base no ÚLTIMO histórico
      const visitanteDoHistorico = {
        documento: placa,
        nome: historico?.nomeVisitante || ultimoRegistro.visitante?.nome || 'Não Informado',
        sobrenome: historico?.sobrenomeVisitante || ultimoRegistro.visitante?.sobrenome || '',
        empresa: ultimoRegistro.visitante?.empresa || 'Não Informada',
        categoria: historico?.nomeCategoriaVisitante || ultimoRegistro.visitante?.categoria || 'VISITANTE',
        codigoCategoria: historico?.codigoCategoriaVisitante || ultimoRegistro.visitante?.codigoCategoria || 16
      };
      
      console.log('🎯 Dados carregados do histórico:', {
        nome: visitanteDoHistorico.nome,
        categoria: visitanteDoHistorico.categoria,
        codigo: visitanteDoHistorico.codigoCategoria
      });
      
      return {
        data: {
          success: true,
          visitante: visitanteDoHistorico,
          ultimasVisitas: response.data.historicos,
          origem: 'historico_direto',
          totalRegistros: response.data.historicos.length
        }
      };
      
    } else {
      console.log('🆕 Placa sem histórico - novo visitante');
      return {
        data: {
          success: false,
          message: 'Nenhum histórico encontrado'
        }
      };
    }
    
  } catch (error) {
    console.log('❌ Erro ao buscar histórico:', error);
    
    // Se der erro 404, trata como placa nova
    if (error.response?.status === 404) {
      console.log('🆕 Placa não encontrada (404) - novo visitante');
      return {
        data: {
          success: false,
          message: 'Placa não encontrada'
        }
      };
    }
    
    return {
      data: {
        success: false,
        message: 'Erro na busca'
      }
    };
  }
}

  //BUSCA INTELIGENTE
  async buscaInteligente(termo) {
    try {
      console.log(`🔍 Busca inteligente para: ${termo}`);
      
      const response = await this.client.get(`/visitantes/busca-inteligente/${encodeURIComponent(termo)}`);
      
      console.log('✅ Resposta busca inteligente:', response.data);
      return response;
    } catch (error) {
      console.log('❌ Erro na busca inteligente:', error);
      
      // Fallback para busca parcial se a inteligente falhar
      try {
        console.log('🔄 Tentando busca parcial como fallback...');
        const response = await this.client.get(`/visitantes/busca/${encodeURIComponent(termo)}`);
        return response;
      } catch (fallbackError) {
        console.log('❌ Fallback também falhou:', fallbackError);
        throw error;
      }
    }
  }

  //REGISTRAR ENTRADA 
  async registrarEntrada(dados) {
    try {
      console.log('📤 ========== INICIANDO REGISTRO ==========');
      console.log('📤 Dados recebidos para registro:', JSON.stringify(dados, null, 2));
      
      // ✅ VALIDAÇÃO DETALHADA DOS DADOS (CONFORME SEU CONTROLLER)
      if (!dados.codigoCategoria) {
        console.log('❌ VALIDAÇÃO FALHOU: Código da categoria é obrigatório');
        throw new Error('Código da categoria é obrigatório');
      }
      
      if (!dados.documento || dados.documento.length < 6) {
        console.log('❌ VALIDAÇÃO FALHOU: Placa inválida -', dados.documento);
        throw new Error('Placa inválida');
      }

      if (!dados.nome || !dados.nome.trim()) {
        console.log('❌ VALIDAÇÃO FALHOU: Nome é obrigatório');
        throw new Error('Nome é obrigatório');
      }

      if (!dados.setor || !dados.setor.trim()) {
        console.log('❌ VALIDAÇÃO FALHOU: Setor é obrigatório');
        throw new Error('Setor é obrigatório');
      }

      if (!dados.unidade || !dados.unidade.trim()) {
        console.log('❌ VALIDAÇÃO FALHOU: Unidade é obrigatória');
        throw new Error('Unidade é obrigatória');
      }

      console.log('✅ Todas as validações passaram');
      console.log('🚀 Enviando requisição POST para /entrada...');

      //ENVIA EXATAMENTE O QUE SEU CONTROLLER ESPERA
      const dadosParaEnviar = {
        documento: dados.documento,
        nome: dados.nome,
        sobrenome: dados.sobrenome || '',
        codigoCategoria: dados.codigoCategoria,
        setor: dados.setor,
        unidade: dados.unidade,
        proprietario: dados.proprietario || 'Não Informado',
        categoria: dados.categoria || 'Visitante',
        observacao: dados.observacao || ''
      };

      const response = await this.client.post('/entrada', dadosParaEnviar);
      
      console.log('✅ RESPOSTA DO SERVIDOR:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      });
      
      return response;
      
    } catch (error) {
      console.log('❌ ========== ERRO NO REGISTRO ==========');
      console.log('❌ Tipo do erro:', error.constructor.name);
      console.log('❌ Mensagem:', error.message);
      console.log('❌ Response status:', error.response?.status);
      console.log('❌ Response data:', error.response?.data);
      console.log('❌ Response headers:', error.response?.headers);
      console.log('❌ Request config:', {
        url: error.config?.url,
        method: error.config?.method,
        data: error.config?.data
      });
      
      if (error.response?.data?.error) {
        const errorMessage = error.response.data.error;
        console.log('❌ Erro específico do servidor:', errorMessage);
        error.message = errorMessage;
      } else if (error.message === 'Network Error') {
        error.message = 'Erro de conexão. Verifique sua internet.';
      } else if (error.code === 'ECONNABORTED') {
        error.message = 'Timeout na conexão com o servidor.';
      }
      
      throw error;
    }
  }

  // MÉTODOS DA API EXISTENTES
  async estatisticas() { 
    return await this.client.get('/estatisticas'); 
  }
  
  // MÉTODO HISTÓRICO BÁSICO
  async historicoVisitante(documento) {
    try {
      console.log(`🔍 Buscando histórico básico para: ${documento}`);
      
      const response = await this.client.get(`/visitantes/${encodeURIComponent(documento)}/historico-completo`);
      
      return response;
    } catch (error) {
      console.log('❌ Erro no histórico básico:', error);
      throw error;
    }
  }
  
  //  MÉTODO HISTÓRICO COMPLETO
  async buscarHistoricoCompleto(documento) {
    try {
      console.log(`🔍 Buscando histórico (limite 5000) para: ${documento}`);
      
      // COM LIMITE DE 5000 PARA PERFORMANCE
      const response = await this.client.get(`/visitantes/${encodeURIComponent(documento)}/historico-completo?limit=4000`);
      
      console.log(`✅ Histórico encontrado: ${response.data.historicos?.length} de ${response.data.totalVisitas} entradas`);
      console.log(`⚡ Limite aplicado: ${response.data.limiteAplicado || 4000}`);
      
      return response;
    } catch (error) {
      console.log('❌ Erro ao buscar histórico:', error);
      
      // Fallback sem limite se der erro
      console.log('🔄 Tentando sem limite...');
      try {
        const response = await this.client.get(`/visitantes/${encodeURIComponent(documento)}/historico-completo`);
        return response;
      } catch (fallbackError) {
        throw error;
      }
    }
  }
  // BUSCAR HISTÓRICOS POR PERÍODO - MÉTODO QUE ESTAVA FALTANDO
async buscarHistoricosPorPeriodo(dataInicio, dataFim, horaInicio = '00:00', horaFim = '23:59') {
  try {
    console.log(`📅 Buscando históricos por período: ${dataInicio} ${horaInicio} até ${dataFim} ${horaFim}`);
    
    // Constrói a URL com os parâmetros
    const params = new URLSearchParams({
      dataInicio: dataInicio,
      dataFim: dataFim,
      horaInicio: horaInicio,
      horaFim: horaFim
    });
    
    const url = `/historicos/por-periodo?${params.toString()}`;
    console.log(`🔗 URL: ${url}`);
    
    const response = await this.client.get(url);
    
    console.log(`✅ ${response.data.historicos?.length || 0} registros encontrados`);
    return response;
    
  } catch (error) {
    console.log('❌ Erro ao buscar históricos por período:', error);
    
    // Fallback: tenta endpoint alternativo sem horários
    try {
      console.log('🔄 Tentando endpoint alternativo...');
      const fallbackUrl = `/historicos/por-periodo?dataInicio=${encodeURIComponent(dataInicio)}&dataFim=${encodeURIComponent(dataFim)}`;
      const fallbackResponse = await this.client.get(fallbackUrl);
      
      console.log(`✅ Fallback: ${fallbackResponse.data.historicos?.length || 0} registros`);
      return fallbackResponse;
      
    } catch (fallbackError) {
      console.log('❌ Fallback também falhou:', fallbackError);
      
      // Se ainda falhar, retorna estrutura vazia para não quebrar a tela
      return {
        data: {
          success: false,
          historicos: [],
          totalRegistros: 0,
          error: 'Nenhum histórico encontrado para o período'
        }
      };
    }
  }
}

  //  MÉTODO PARA OBTER NOVO TOKEN
  async getNewToken() {
    try {
      console.log('🔄 Solicitando novo token...');
      const response = await this.client.get('/token');
      
      if (response.data && response.data.token) {
        await this.setToken(response.data.token);
        return response.data.token;
      } else {
        throw new Error('Token não recebido da API');
      }
    } catch (error) {
      console.log('❌ Erro ao obter novo token:', error);
      throw error;
    }
  }

  async ensureToken() {
    if (this.token) return this.token;
    return await this.getNewToken();
  }
  
  async healthCheck() {
    try {
      const response = await axios.get(this.currentBaseURL + '/health', { timeout: 10000 });
      return { success: true, data: response.data };
    } catch (error) {
      return { 
        success: false, 
        error: error.message,
        status: error.response?.status 
      };
    }
  }

  async testToken() {
    try {
      const response = await this.client.get('/estatisticas');
      return { valid: true, data: response.data };
    } catch (error) {
      return { valid: false, error: error.message };
    }
  }

  //GETTERS E SETTERS
  getCurrentBaseURL() { 
    return this.currentBaseURL; 
  }
  
  hasValidToken() { 
    return this.isAuthenticated && this.token; 
  }
  
  async setToken(token) {
    this.token = token;
    this.isAuthenticated = true;
    await AsyncStorage.setItem('auth_token', token);
    console.log('🔑 Token configurado:', token.substring(0, 10) + '...');
  }

  async clearToken() {
    this.token = null;
    this.isAuthenticated = false;
    await AsyncStorage.removeItem('auth_token');
    console.log('🔑 Token removido');
  }
}

const apiClient = new ApiClient();
export default apiClient;