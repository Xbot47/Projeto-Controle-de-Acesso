import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

// Screens
import HomeScreen from '../screens/HomeScreen';
import RegisterScreen from '../screens/RegisterScreen';
import SearchScreen from '../screens/SearchScreen';
import Header from '../components/Header';
import AdvancedSearchScreen from '../screens/AdvancedSearchScreen';

const Stack = createNativeStackNavigator();

const AppNavigator = () => {
  return (
    <Stack.Navigator initialRouteName="Home">
      <Stack.Screen
        name="Home"
        component={HomeScreen}
        options={({ navigation }) => ({
          header: () => (
            <Header
              title="Controle de Acesso"
              showStatus={true}
              onRefresh={() => navigation.replace('Home')}
            />
          )
        })}
      />
      <Stack.Screen 
        name="AdvancedSearch" 
        component={AdvancedSearchScreen}
        options={{ 
          title: 'Busca Avançada',
          headerBackTitle: 'Voltar'
        }}
      />
      <Stack.Screen
        name="Register"
        component={RegisterScreen}
        options={{ 
          title: 'Registrar Entrada',
          headerBackTitle: 'Voltar'
        }}
      />
      
      <Stack.Screen
        name="Search"
        component={SearchScreen}
        options={{ 
          title: 'Buscar Histórico',
          headerBackTitle: 'Voltar'
        }}
      />
    </Stack.Navigator>
  );
};

export default AppNavigator;