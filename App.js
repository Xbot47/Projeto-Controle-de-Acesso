// App.js (na raiz do projeto)
import React from 'react';
import { StyleSheet, ScrollView } from 'react-native';
import MeuCard from './src/componentes/MeuCard';

export default function App() {
  return (
    <ScrollView contentContainerStyle={styles.container}>
      <MeuCard/>
      <MeuCard/>
      <MeuCard/>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingTop: 20,
    paddingBottom: 40,
    paddingHorizontal: 10,
  },
});
