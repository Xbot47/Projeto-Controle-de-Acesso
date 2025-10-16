import React, { createContext, useState, useContext, useEffect } from 'react';
import apiClient from '../api/apiClient';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [serverStatus, setServerStatus] = useState('checking');
  const [lastChecked, setLastChecked] = useState(null);

  const checkSystemStatus = async () => {
    setIsLoading(true);
    console.log('🔄 Iniciando verificação do sistema...');
    
    try {
      // PRIMEIRO: Tenta carregar token salvo
      await apiClient.loadInitialToken();
      
      // SEGUNDO: Testa a conexão com a API (health check)
      console.log('🌐 Testando conexão com a API...');
      const health = await apiClient.healthCheck();
      
      if (health.success) {
        setServerStatus('online');
        console.log('✅ Servidor ONLINE');
        
        // TERCEIRO: Testa se o token é válido
        if (apiClient.hasValidToken()) {
          console.log('🔑 Token presente, testando validade...');
          const tokenTest = await apiClient.testToken();
          
          if (tokenTest.valid) {
            setIsAuthenticated(true);
            console.log('✅ Token VÁLIDO - Sistema autenticado');
          } else {
            setIsAuthenticated(false);
            console.log('❌ Token INVÁLIDO:', tokenTest.error);
            
            // Tenta obter novo token automaticamente
            try {
              console.log('🔄 Tentando obter novo token...');
              await apiClient.getNewToken();
              setIsAuthenticated(true);
              console.log('✅ Novo token obtido com sucesso!');
            } catch (tokenError) {
              console.log('❌ Falha ao obter novo token:', tokenError.message);
              setIsAuthenticated(false);
            }
          }
        } else {
          // Não tem token válido
          setIsAuthenticated(false);
          console.log('❌ Nenhum token válido disponível');
        }
      } else {
        // Servidor offline
        setServerStatus('offline');
        setIsAuthenticated(false);
        console.log('❌ Servidor OFFLINE:', health.error);
      }

    } catch (error) {
      // Erro geral
      console.log('💥 Erro geral no checkSystemStatus:', error.message);
      setServerStatus('offline');
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
      setLastChecked(new Date());
      console.log('📊 Status final:', {
        servidor: serverStatus,
        autenticado: isAuthenticated,
        carregando: false
      });
    }
  };

  const refreshStatus = async () => {
    console.log('🔄 Atualizando status manualmente...');
    await checkSystemStatus();
  };
  
  const login = async () => {
    console.log('🔐 Iniciando processo de login...');
    await checkSystemStatus();
    return apiClient.hasValidToken();
  }

  const logout = async () => {
    console.log('🚪 Realizando logout...');
    await apiClient.clearToken();
    setIsAuthenticated(false);
    setServerStatus('offline');
  }

  // Verificação automática periódica (a cada 2 minutos)
  useEffect(() => {
    checkSystemStatus();

    // Configura verificação periódica
    const interval = setInterval(() => {
      if (!isLoading) {
        console.log('⏰ Verificação periódica do status...');
        checkSystemStatus();
      }
    }, 120000); // 2 minutos

    return () => clearInterval(interval);
  }, []);

  // Verificação quando o app volta do background (opcional)
  useEffect(() => {
    const handleAppStateChange = (nextAppState) => {
      if (nextAppState === 'active' && lastChecked) {
        const minutesSinceLastCheck = (new Date() - lastChecked) / (1000 * 60);
        if (minutesSinceLastCheck > 5) { // Mais de 5 minutos
          console.log('📱 App retornou do background, verificando status...');
          checkSystemStatus();
        }
      }
    };
  }, [lastChecked]);

  return (
    <AuthContext.Provider value={{
      isAuthenticated,
      isLoading,
      serverStatus,
      lastChecked,
      refreshStatus,
      checkSystemStatus,
      login,
      logout
    }}>
      {children}
    </AuthContext.Provider>
  );
};