import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  RefreshControl,
  Alert
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import styles from '../styles/theme';

export default function HomeScreen({ navigation }) {
  const { isAuthenticated, isLoading, serverStatus, refreshStatus } = useAuth();
  const [refreshing, setRefreshing] = React.useState(false);

  const getStatusColor = () => {
    if (serverStatus === 'online' && isAuthenticated) return '#2ecc71';
    if (serverStatus === 'online') return '#f39c12';
    return '#e74c3c';
  };

  const getStatusText = () => {
    if (serverStatus === 'online' && isAuthenticated) return '✅ Conectado e Autenticado';
    if (serverStatus === 'online') return '⚠️ Conectado, mas Sem Token';
    if (serverStatus === 'checking') return '🔄 Verificando...';
    return '❌ Offline';
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    await refreshStatus();
    setRefreshing(false);
  };

  const StatusIndicator = () => (
    <View style={styles.statusContainer}>
      <Text style={styles.statusTitle}>Status da API</Text>
      
      <View style={styles.statusRow}>
        <View style={[styles.statusIndicator, { backgroundColor: getStatusColor() }]} />
        <Text style={styles.statusText}>{getStatusText()}</Text>
      </View>

      <Text style={styles.statusDetail}>
        • Servidor: {serverStatus === 'online' ? '✅ Online' : '❌ Offline'}
      </Text>
      <Text style={styles.statusDetail}>
        • Token: {isAuthenticated ? '✅ Válido' : '❌ Inválido'}
      </Text>

      <TouchableOpacity 
        style={styles.smallButton} 
        onPress={handleRefresh}
        disabled={isLoading}
      >
        <Text style={styles.smallButtonText}>
          {isLoading ? '🔄 Atualizando...' : '🔄 Atualizar Status'}
        </Text>
      </TouchableOpacity>
    </View>
  );

  const MenuItem = ({ title, description, icon, action, color }) => (
    <TouchableOpacity
      style={[styles.menuCard, { borderLeftColor: color }]}
      onPress={() => navigation.navigate(action)}
      disabled={!isAuthenticated}
    >
      <Text style={styles.menuIcon}>{icon}</Text>
      <View style={styles.menuContent}>
        <Text style={[styles.menuTitle, !isAuthenticated && {color: '#bdc3c7'}]}>
          {title}
        </Text>
        <Text style={styles.menuDescription}>{description}</Text>
        {!isAuthenticated && (
          <Text style={{color: '#e74c3c', fontSize: 12, marginTop: 4}}>
            ⚠️ Sistema offline
          </Text>
        )}
      </View>
    </TouchableOpacity>
  );

  const menuItems = [
    {
      title: '📝 Registrar Entrada (Placa)',
      description: 'Cadastrar novo veículo ou visitante',
      icon: '🚗',
      action: 'Register',
      color: '#4CAF50'
    },
    {
      title: '🔍 Buscar Histórico',
      description: 'Buscar placa por documento ou placa e ver histórico',
      icon: '🔎',
      action: 'Search',
      color: '#2196F3'
    },
    {
    title: '🔍 Busca Avançada',
    description: 'Buscar por período, empresa e listagens completas',
    icon: '🎯',
    action: 'AdvancedSearch',
    color: '#9C27B0'
   },
  ];

  return (
    <ScrollView 
      style={styles.container}
      contentContainerStyle={{ paddingBottom: 30 }}
      refreshControl={
        <RefreshControl 
          refreshing={refreshing} 
          onRefresh={handleRefresh}
          colors={['#3498db']}
          tintColor="#3498db"
        />
      }
    >
      <StatusIndicator />
      
      <View style={styles.menuSection}>
        <Text style={styles.sectionTitle}>Funcionalidades</Text>
        {menuItems.map((item, index) => (
          <MenuItem
            key={index}
            title={item.title}
            description={item.description}
            icon={item.icon}
            action={item.action}
            color={item.color}
          />
        ))}
      </View>
      
      <View style={styles.infoBox}>
        <Text style={styles.statusTitle}>💡 Dicas</Text>
        <Text style={styles.statusDetail}>
          • Use o campo Documento para inserir a placa do veículo (Ex: ABC1234).
        </Text>
        <Text style={styles.statusDetail}>
          • Setor e Unidade são campos obrigatórios para o registro.
        </Text>
        {!isAuthenticated && (
          <Text style={[styles.statusDetail, {color: '#e74c3c'}]}>
            • ⚠️ Sistema offline - algumas funcionalidades estão desabilitadas
          </Text>
        )}
      </View>
      
    </ScrollView>
  );
}