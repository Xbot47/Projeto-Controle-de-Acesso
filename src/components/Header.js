import React from 'react';
import { View, Text, TouchableOpacity, StatusBar } from 'react-native';
import { useAuth } from '../context/AuthContext';
import styles from '../styles/theme';

const Header = ({ title, showStatus = true, onRefresh }) => {
  // Puxa o estado de autenticação para exibir o status
  const { isAuthenticated, refreshStatus } = useAuth(); 

  const handleRefresh = () => {
    // onRefresh é usado na tela Home para forçar a navegação (recarregar)
    if (onRefresh) {
      onRefresh(); 
    } else {
      // Para telas internas, apenas revalida o status de conexão
      refreshStatus(); 
    }
  };

  return (
    <View style={styles.headerBar}>
      {/* Define a cor da barra de status do sistema */}
      <StatusBar backgroundColor="#3498db" barStyle="light-content" />
      
      <View style={styles.headerContent}>
        <Text style={styles.headerTitle}>{title}</Text>
        
        {/* Mostra o indicador de status de conexão (Online/Offline) */}
        {showStatus && (
          <View style={styles.headerStatus}>
            <View style={[
              styles.statusIndicator, 
              // Se autenticado, é verde; senão, vermelho
              { backgroundColor: isAuthenticated ? '#2ecc71' : '#e74c3c' }
            ]} />
            <Text style={styles.statusTextHeader}>
              {isAuthenticated ? 'Online' : 'Offline'}
            </Text>
          </View>
        )}
      </View>

      {/* Botão de atualização manual, visível na Home */}
      {onRefresh && (
        <TouchableOpacity style={styles.refreshButton} onPress={handleRefresh}>
          <Text style={styles.refreshText}>🔄</Text>
        </TouchableOpacity>
      )}
    </View>
  );
};

export default Header;
