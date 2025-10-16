package com.api.apitest.services;

import com.api.apitest.entities.CategoriasVisitantes;
import com.api.apitest.entities.Historico;
import com.api.apitest.entities.HistoricoVisitado;
import com.api.apitest.entities.Visitante;
import com.api.apitest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VisitanteService {

    @Autowired
    private VisitanteRepository visitanteRepository;

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private HistoricoVisitadoRepository historicoVisitadoRepository;

    @Autowired
    private CategoriasVisitantesRepository categoriasVisitantesRepository;

    @Transactional
    public Visitante registrarEntradaCompleta(String documento, String nome, String sobrenome,
                                              String categoria, String setor, String unidade,
                                              String proprietario) {

        System.out.println("🔵 ========== [VISITANTE SERVICE] INICIANDO REGISTRO ==========");
        System.out.println("🔵 Dados recebidos:");
        System.out.println("🔵 - Documento: " + documento);
        System.out.println("🔵 - Nome: " + nome);
        System.out.println("🔵 - Sobrenome: " + sobrenome);
        System.out.println("🔵 - Categoria: " + categoria);
        System.out.println("🔵 - Setor: " + setor);
        System.out.println("🔵 - Unidade: " + unidade);
        System.out.println("🔵 - Proprietário: " + proprietario);

        try {
            // 1. Normalizar documento
            String doc = documento.trim().toUpperCase().replace(" ", "").replace("-", "").replace(".", "");
            System.out.println("✅ Documento normalizado: " + doc);

            if (doc.length() > 30) {
                throw new IllegalArgumentException("Documento muito longo (máx. 30 caracteres): " + doc);
            }

            // 2. Buscar ou criar categoria
            System.out.println("🔍 Processando categoria...");
            Integer codigoCategoria = buscarOuCriarCategoria(categoria);
            System.out.println("✅ Código categoria determinado: " + codigoCategoria);

            // 3. Verificar visitante existente
            System.out.println("🔍 Buscando visitante no banco: " + doc);
            Optional<Visitante> visitanteOpt = visitanteRepository.findByDocumento(doc);
            Visitante visitante;

            if (visitanteOpt.isPresent()) {
                System.out.println("✅ Visitante existente encontrado");
                visitante = visitanteOpt.get();
                int visitasAnteriores = visitante.getNumeroVisitas() != null ? visitante.getNumeroVisitas() : 0;
                visitante.setNumeroVisitas(visitasAnteriores + 1);
                visitante.setCodigoCategoriasVisitantes(codigoCategoria);
                visitante.setDataHora(LocalDateTime.now());

                // Atualizar dados se fornecidos
                if (nome != null && !nome.trim().isEmpty()) {
                    visitante.setNome(nome);
                }
                if (sobrenome != null && !sobrenome.trim().isEmpty()) {
                    visitante.setSobrenome(sobrenome);
                }

                System.out.println("📊 Visitante atualizado - Visitas: " + (visitasAnteriores + 1));
            } else {
                System.out.println("🆕 Criando NOVO visitante");
                visitante = new Visitante();
                visitante.setDocumento(doc);
                visitante.setNome(nome);
                visitante.setSobrenome(sobrenome != null ? sobrenome : "");
                visitante.setCodigoCategoriasVisitantes(codigoCategoria);

                // ✅ CORREÇÃO: Campos obrigatórios com valores padrão conforme estrutura real
                visitante.setNumeroVisitas(1);
                visitante.setEpi(false);
                visitante.setVip(false);
                visitante.setDeficiente(false);
                visitante.setCatStatusVisitante("N");
                visitante.setCatBioTECF7StatusEnvioValidade("N");
                visitante.setIntegracaoControle("N/A");
                visitante.setDataHora(LocalDateTime.now());

                // ✅ CORREÇÃO: Campos que podem ser nulos mas vamos inicializar
                visitante.setCodigoHistoricos(null);
                visitante.setCodigoHistoricosFotosVisitantes(null);
                visitante.setObservacao("Registro via API Mobile");

                System.out.println("📝 Novo visitante criado na memória");
            }

            // 4. Salvar visitante
            System.out.println("💾 Salvando visitante no banco...");
            Visitante visitanteSalvo = visitanteRepository.save(visitante);
            System.out.println("✅ Visitante salvo com sucesso!");
            System.out.println("   - Documento: " + visitanteSalvo.getDocumento());
            System.out.println("   - Nome: " + visitanteSalvo.getNome());
            System.out.println("   - Visitas: " + visitanteSalvo.getNumeroVisitas());

            // 5. Criar histórico
            System.out.println("📝 Criando registro de histórico...");
            Historico historico = new Historico();
            historico.setDocumentoVisitante(doc);
            historico.setNomeVisitante(nome);
            historico.setSobrenomeVisitante(sobrenome != null ? sobrenome : "");
            historico.setNomeCategoriaVisitante(categoria != null ? categoria : "VISITANTE");
            historico.setCodigoCategoriaVisitante(codigoCategoria);
            historico.setDataHoraEntrada(LocalDateTime.now());

            // ✅ CORREÇÃO: Definir pessoa visitada
            String nomeVisitado = "Não Informado";
            String documentoVisitado = "00000000000";

            if (proprietario != null && !proprietario.trim().isEmpty()) {
                nomeVisitado = proprietario;
                String numeros = proprietario.replaceAll("[^0-9]", "");
                if (!numeros.isEmpty()) {
                    documentoVisitado = numeros.length() > 11 ? numeros.substring(0, 11) : numeros;
                }
            } else {
                nomeVisitado = "Responsável " + setor;
            }

            historico.setNomeVisitadoUsuarioEntrada(nomeVisitado);
            historico.setNomeUsuarioEntrada("Sistema Mobile");
            historico.setDocumentoVisitadoUsuarioEntrada(documentoVisitado);

            Historico historicoSalvo = historicoRepository.save(historico);
            System.out.println("✅ Histórico criado com sucesso!");
            System.out.println("   - Código Histórico: " + historicoSalvo.getCodigo());
            System.out.println("   - Data/Hora: " + historicoSalvo.getDataHoraEntrada());

            // 6. Registrar destino - ✅ AGORA É OBRIGATÓRIO
            System.out.println("📍 Registrando destino da visita...");
            try {
                registrarDestinoCompleto(doc, historicoSalvo.getCodigo(), setor, unidade, proprietario);
                System.out.println("✅ Destino registrado com sucesso!");
            } catch (Exception e) {
                System.err.println("💥 ERRO CRÍTICO ao registrar destino: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Falha ao registrar destino: " + e.getMessage(), e);
            }

            System.out.println("🎉 ========== [VISITANTE SERVICE] REGISTRO CONCLUÍDO COM SUCESSO! ==========");
            return visitanteSalvo;

        } catch (Exception e) {
            System.err.println("💥 ========== [VISITANTE SERVICE] ERRO NO REGISTRO ==========");
            System.err.println("💥 Mensagem: " + e.getMessage());
            System.err.println("💥 Tipo: " + e.getClass().getName());
            e.printStackTrace();
            throw new RuntimeException("Erro ao registrar entrada: " + e.getMessage(), e);
        }
    }

    // ✅ MÉTODO MELHORADO: Registrar destino completo
    private void registrarDestinoCompleto(String documento, Long codigoHistorico, String setor, String unidade, String proprietario) {
        System.out.println("🔵 [DESTINO] Iniciando registro de destino...");

        try {
            // Validar dados obrigatórios
            if (setor == null || setor.trim().isEmpty()) {
                throw new IllegalArgumentException("Setor é obrigatório para registro de destino");
            }
            if (unidade == null || unidade.trim().isEmpty()) {
                throw new IllegalArgumentException("Unidade é obrigatória para registro de destino");
            }

            // ✅ CORREÇÃO: Garantir que setor e unidade não ultrapassem 30 caracteres
            String setorFormatado = setor.length() > 30 ? setor.substring(0, 30) : setor;
            String unidadeFormatada = unidade.length() > 30 ? unidade.substring(0, 30) : unidade;

            // Definir informações da pessoa visitada
            String nomeVisitado;
            String sobrenomeVisitado;
            String documentoVisitado;

            if (proprietario != null && !proprietario.trim().isEmpty()) {
                String[] partes = proprietario.split(" ", 2);
                nomeVisitado = partes[0];
                sobrenomeVisitado = partes.length > 1 ? partes[1] : "";

                // ✅ CORREÇÃO: Limitar tamanho dos campos
                nomeVisitado = nomeVisitado.length() > 60 ? nomeVisitado.substring(0, 60) : nomeVisitado;
                sobrenomeVisitado = sobrenomeVisitado.length() > 30 ? sobrenomeVisitado.substring(0, 30) : sobrenomeVisitado;

                String numeros = proprietario.replaceAll("[^0-9]", "");
                documentoVisitado = numeros.isEmpty() ? "00000000000" :
                        numeros.length() > 30 ? numeros.substring(0, 30) : numeros;
            } else {
                nomeVisitado = "Responsável";
                sobrenomeVisitado = setorFormatado;
                documentoVisitado = "00000000000";
            }

            System.out.println("🔵 [DESTINO] Criando entidade HistoricoVisitado...");
            HistoricoVisitado visitado = new HistoricoVisitado();

            // ✅ Campos obrigatórios da chave primária
            visitado.setDocumentoVisitado(documentoVisitado);
            visitado.setCodigoHistoricos(codigoHistorico);

            // ✅ Informações do destino
            visitado.setNomeSetorVisitado(setorFormatado);
            visitado.setNomeUnidadeVisitado(unidadeFormatada);

            // ✅ Informações da pessoa visitada
            visitado.setNomeVisitado(nomeVisitado);
            visitado.setSobrenomeVisitado(sobrenomeVisitado);

            // ✅ Campos adicionais
            visitado.setVisitadoPrincipal(true);
            visitado.setMensagemVisitado("Registro via API Mobile - " + LocalDateTime.now());

            // ✅ Campos com valores padrão
            visitado.setCodigoParticaoVisitado(1);
            visitado.setNomeParticaoVisitado("Principal");
            visitado.setCodigoUnidadeVisitado(1);
            visitado.setCodigoSetorVisitado(1);

            System.out.println("💾 [DESTINO] Salvando destino no banco...");
            HistoricoVisitado salvo = historicoVisitadoRepository.save(visitado);

            System.out.println("✅ [DESTINO] Destino salvo com sucesso!");
            System.out.println("   - Documento Visitado: " + salvo.getDocumentoVisitado());
            System.out.println("   - Código Histórico: " + salvo.getCodigoHistoricos());
            System.out.println("   - Pessoa: " + nomeVisitado + " " + sobrenomeVisitado);
            System.out.println("   - Local: " + setorFormatado + " / " + unidadeFormatada);

        } catch (Exception e) {
            System.err.println("💥 [DESTINO] ERRO ao registrar destino:");
            System.err.println("💥 Mensagem: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ✅ MÉTODO MELHORADO: Buscar ou criar categoria
    private Integer buscarOuCriarCategoria(String nomeCategoria) {
        System.out.println("🔍 [CATEGORIA] Processando categoria: " + nomeCategoria);

        try {
            String nome = (nomeCategoria != null && !nomeCategoria.trim().isEmpty()) ?
                    nomeCategoria.trim() : "VISITANTE";

            System.out.println("🔍 [CATEGORIA] Buscando categoria: " + nome);

            // ✅ CORREÇÃO: Buscar usando método correto do repository
            List<CategoriasVisitantes> todasCategorias = categoriasVisitantesRepository.findAll();
            System.out.println("📊 [CATEGORIA] Total de categorias no sistema: " + todasCategorias.size());

            if (todasCategorias.isEmpty()) {
                System.out.println("⚠️ [CATEGORIA] Nenhuma categoria encontrada, usando padrão 16 (VISITANTE)");
                return 16; // Código padrão para VISITANTE
            }

            // Tentar encontrar categoria exata
            Optional<CategoriasVisitantes> categoriaExata = todasCategorias.stream()
                    .filter(c -> c.getNome() != null && c.getNome().equalsIgnoreCase(nome))
                    .findFirst();

            if (categoriaExata.isPresent()) {
                Long codigo = categoriaExata.get().getCodigo();
                System.out.println("✅ [CATEGORIA] Categoria exata encontrada: " + codigo + " - " + nome);
                return codigo.intValue();
            }

            // ✅ CORREÇÃO: Buscar por mapeamento de nomes comuns
            Integer codigoMapeado = mapearCategoriaPorNome(nome);
            if (codigoMapeado != null) {
                System.out.println("✅ [CATEGORIA] Categoria mapeada: " + codigoMapeado + " - " + nome);
                return codigoMapeado;
            }

            // Tentar encontrar categoria similar
            Optional<CategoriasVisitantes> categoriaSimilar = todasCategorias.stream()
                    .filter(c -> c.getNome() != null && c.getNome().toLowerCase().contains(nome.toLowerCase()))
                    .findFirst();

            if (categoriaSimilar.isPresent()) {
                Long codigo = categoriaSimilar.get().getCodigo();
                System.out.println("✅ [CATEGORIA] Categoria similar encontrada: " + codigo + " - " + categoriaSimilar.get().getNome());
                return codigo.intValue();
            }

            // Usar primeira categoria disponível
            CategoriasVisitantes primeiraCategoria = todasCategorias.get(0);
            Long codigo = primeiraCategoria.getCodigo();
            System.out.println("⚠️ [CATEGORIA] Usando primeira categoria disponível: " + codigo + " - " + primeiraCategoria.getNome());
            return codigo.intValue();

        } catch (Exception e) {
            System.err.println("❌ [CATEGORIA] Erro ao processar categoria: " + e.getMessage());
            System.out.println("⚠️ [CATEGORIA] Usando categoria padrão: 16 (VISITANTE)");
            return 16; // Fallback para VISITANTE
        }
    }

    // ✅ NOVO MÉTODO: Mapear categoria por nome comum
    private Integer mapearCategoriaPorNome(String nomeCategoria) {
        String nomeUpper = nomeCategoria.toUpperCase();

        // Mapeamento baseado nos códigos que vimos anteriormente
        if (nomeUpper.contains("ENTREGA")) return 10;
        if (nomeUpper.contains("VISITANTE")) return 16;
        if (nomeUpper.contains("FUNCIONÁRIO") || nomeUpper.contains("FUNCIONARIO")) return 17;
        if (nomeUpper.contains("MORADOR")) return 15;
        if (nomeUpper.contains("IFOOD") || nomeUpper.contains("DELIVERY") || nomeUpper.contains("RAPPI")) return 6;
        if (nomeUpper.contains("MOTO") || nomeUpper.contains("MOTORISTA")) return 5;
        if (nomeUpper.contains("SERVIÇO") || nomeUpper.contains("OBRA")) return 12;
        if (nomeUpper.contains("TÁXI") || nomeUpper.contains("TAXI")) return 3;
        if (nomeUpper.contains("UBER") || nomeUpper.contains("99") || nomeUpper.contains("INDRIVE")) return 18;

        return null;
    }

    public Optional<Visitante> buscarPorDocumento(String documento) {
        try {
            String doc = documento.trim().toUpperCase().replace(" ", "");
            System.out.println("🔍 [SERVICE] Buscando visitante: " + doc);

            Optional<Visitante> visitante = visitanteRepository.findByDocumento(doc);

            if (visitante.isPresent()) {
                System.out.println("✅ [SERVICE] Visitante encontrado: " + visitante.get().getNome());
            } else {
                System.out.println("❌ [SERVICE] Visitante não encontrado: " + doc);
            }

            return visitante;
        } catch (Exception e) {
            System.err.println("💥 [SERVICE] Erro ao buscar visitante: " + e.getMessage());
            return Optional.empty();
        }
    }

    public long contarVisitasHoje() {
        try {
            return historicoRepository.countTodayEntries();
        } catch (Exception e) {
            System.err.println("❌ Erro ao contar visitas hoje: " + e.getMessage());
            return 0;
        }
    }

    public long contarTotalVisitantes() {
        try {
            return visitanteRepository.count();
        } catch (Exception e) {
            System.err.println("❌ Erro ao contar total de visitantes: " + e.getMessage());
            return 0;
        }
    }

    public long contarTotalEntradas() {
        try {
            return historicoRepository.count();
        } catch (Exception e) {
            System.err.println("❌ Erro ao contar total de entradas: " + e.getMessage());
            return 0;
        }
    }
}