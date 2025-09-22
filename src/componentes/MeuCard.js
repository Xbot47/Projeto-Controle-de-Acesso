import React, { useState } from 'react';
import { StyleSheet, Text, TextInput, View, Image, TouchableOpacity } from 'react-native';
import * as ImagePicker from 'expo-image-picker';


// Usa imagem padrão do assets caso nenhuma seja fornecida
const defaultImg = require('../../assets/favicon.png');

// Componente MeuCard
export default function MeuCard({ foto }) {
  const [imageUri, setImageUri] = useState(null);
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');

  // Função para escolher imagem da galeria
  const escolherImagem = async () => {
    try {
      const result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (!result.canceled && result.assets && result.assets.length > 0) {
        setImageUri(result.assets[0].uri);
      }
    } catch (e) {
      console.log('Erro image picker:', e);
    }
  };
    // Função para determinar a fonte da imagem
  const getImageSource = () => {
    if (imageUri) {
      return { uri: imageUri };
    }
    if (foto) {
      return typeof foto === 'string' ? { uri: foto } : foto;
    }
    return defaultImg;
  };

  // Função aprimorada para capitalizar cada palavra do nome
  const handleNomeChange = (text) => {
    if (text.length > 0) {
      // Divide o texto em palavras
      const words = text.split(' ');
      
      // Itera sobre as palavras, capitalizando a primeira letra de cada uma
      const capitalizedWords = words.map(word => {
        if (word.length > 0) {
          return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
        }
        return '';
      });
      
      // Junta as palavras novamente e atualiza o estado
      setNome(capitalizedWords.join(' '));
    } else {
      setNome('');
    }
  };

    const handleEmailChange = (text) => {
      // Converte a string inteira para minúsculas
        const lowerCaseText = text.toLowerCase().replace(/ /g, ''); // Esse "replace"remove espaços
     // Atualiza o estado
        setEmail(lowerCaseText);
    };

  return (
    <View style={styles.cardContainer}>
      <TouchableOpacity onPress={escolherImagem} style={styles.imagemContainer}>
        <Image style={styles.imagem} source={getImageSource()} />
      </TouchableOpacity>

      <View style={styles.textoContainer}>
        {/* Linha do Nome */}
        <View style={styles.linhaInfo}>
          <Text style={styles.label}>Nome:</Text>
          <TextInput
            style={styles.input}
            value={nome}
            onChangeText={handleNomeChange} // Usa a função aprimorada
          />
        </View>

        {/* Linha do E-mail */}
        <View style={styles.linhaInfo}>
          <Text style={styles.label}>E-mail:</Text>
          <TextInput
            style={styles.input}
            value={email}
            onChangeText={handleEmailChange}
            keyboardType="email-address"
          />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  cardContainer: {
    borderWidth: 1,
    flexDirection: 'row',
    height: 120,
    marginHorizontal: 10,
    backgroundColor: 'cyan',
    marginBottom: 10,
    padding: 10,
    borderRadius: 6,
  },
  imagemContainer: {
    width: 80,
    height: 80,
    borderRadius: 40,
    overflow: 'hidden',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#90acddff',
    marginRight: 12,
  },
  imagem: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  textoContainer: {
    flex: 1,
    justifyContent: 'center',
  },
  linhaInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  label: {
    fontSize: 16,
    marginRight: 5,
    fontWeight: 'bold',
    color: '#333',
  },
  input: {
    flex: 1,
    fontSize: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#ccc',
    paddingVertical: 0,
    paddingHorizontal: 5,
    color: '#333',
  },
});