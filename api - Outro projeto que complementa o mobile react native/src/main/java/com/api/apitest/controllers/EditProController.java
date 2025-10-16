package com.api.apitest.controllers;

import com.api.apitest.dto.RegistroEntradaDTO;
import com.api.apitest.entities.Historico;
import com.api.apitest.entities.Visitante;
import com.api.apitest.repository.CategoriasVisitantesRepository;
import com.api.apitest.repository.HistoricoRepository;
import com.api.apitest.repository.HistoricoVisitadoRepository;
import com.api.apitest.repository.VisitanteRepository;
import com.api.apitest.security.TokenGenerator;
import com.api.apitest.services.VisitanteService;
import com.api.apitest.entities.HistoricoVisitado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/editpro")
@CrossOrigin(origins = "*")
public class EditProController {

    @Autowired
    private VisitanteRepository visitanteRepository;

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private CategoriasVisitantesRepository categoriaVisitanteRepository;

    @Autowired
    private VisitanteService visitanteService;

    @Autowired
    private HistoricoVisitadoRepository historicoVisitadoRepository;

    // ============================================================
    // 🔒 MÉTODOS DE AUTENTICAÇÃO
    // ============================================================

    private boolean validarToken(String token) {
        if (token == null || token.isBlank()) return false;
        String tokenEsperado = TokenGenerator.generateWeeklyToken();
        return token.equals(tokenEsperado);
    }

    private String extrairToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7);
    }

    // ============================================================
    // 🔓 ENDPOINT PÚBLICO - Gerar Token
    // ============================================================
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> obterToken() {
        String token = TokenGenerator.generateWeeklyToken();
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Token gerado com sucesso");
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 🩺 HEALTH CHECK
    // ============================================================
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "EditPro API");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 🟩 REGISTRAR ENTRADA
    // ============================================================
    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody RegistroEntradaDTO registro,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("🔵 ========== NOVA REQUISIÇÃO DE REGISTRO ==========");
        System.out.println("🔵 Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("🔵 Authorization Header: " + authHeader);

        // 🔒 Validar token
        String token = extrairToken(authHeader);
        System.out.println("🔵 Token extraído: " + (token != null ? "PRESENTE" : "AUSENTE"));

        if (!validarToken(token)) {
            System.out.println("❌ FALHA NA AUTENTICAÇÃO: Token inválido ou expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }
        System.out.println("✅ Token válido");

        // ✅ Validação detalhada com logs
        System.out.println("🔵 Validando dados recebidos...");

        if (registro.getDocumento() == null || registro.getDocumento().trim().isEmpty()) {
            System.out.println("❌ VALIDAÇÃO FALHOU: Documento (placa) é obrigatório");
            return ResponseEntity.badRequest().body(criarRespostaErro("Documento (placa) é obrigatório"));
        }
        System.out.println("✅ Documento: " + registro.getDocumento());

        if (registro.getNome() == null || registro.getNome().trim().isEmpty()) {
            System.out.println("❌ VALIDAÇÃO FALHOU: Nome é obrigatório");
            return ResponseEntity.badRequest().body(criarRespostaErro("Nome é obrigatório"));
        }
        System.out.println("✅ Nome: " + registro.getNome());

        if (registro.getSetor() == null || registro.getSetor().trim().isEmpty()) {
            System.out.println("❌ VALIDAÇÃO FALHOU: Setor é obrigatório");
            return ResponseEntity.badRequest().body(criarRespostaErro("Setor é obrigatório"));
        }
        System.out.println("✅ Setor: " + registro.getSetor());

        if (registro.getUnidade() == null || registro.getUnidade().trim().isEmpty()) {
            System.out.println("❌ VALIDAÇÃO FALHOU: Unidade é obrigatória");
            return ResponseEntity.badRequest().body(criarRespostaErro("Unidade é obrigatória"));
        }
        System.out.println("✅ Unidade: " + registro.getUnidade());

        // ✅ Log dos dados opcionais
        System.out.println("🔵 Dados opcionais:");
        System.out.println("🔵 - Sobrenome: " + registro.getSobrenome());
        System.out.println("🔵 - Categoria: " + registro.getCategoria());
        System.out.println("🔵 - Proprietário: " + registro.getProprietario());
        System.out.println("🔵 - Código Categoria: " + registro.getCodigoCategoria());

        try {
            // ✅ Normalizar documento (placa)
            String documentoNormalizado = registro.getDocumento()
                    .trim()
                    .toUpperCase()
                    .replace(" ", "")
                    .replace("-", "")
                    .replace(".", "");

            System.out.println("🔄 Processando registro para placa: " + documentoNormalizado);
            System.out.println("🔄 Chamando visitanteService.registrarEntradaCompleta...");

            Visitante visitante = visitanteService.registrarEntradaCompleta(
                    documentoNormalizado,
                    registro.getNome(),
                    registro.getSobrenome() != null ? registro.getSobrenome() : "",
                    registro.getCategoria() != null ? registro.getCategoria() : "Visitante",
                    registro.getSetor(),
                    registro.getUnidade(),
                    registro.getProprietario() != null ? registro.getProprietario() : "Não Informado"
            );

            System.out.println("✅ Registro concluído com sucesso!");
            System.out.println("✅ Visitante: " + visitante.getDocumento() + " - " + visitante.getNome());
            System.out.println("✅ Número de visitas: " + visitante.getNumeroVisitas());
            System.out.println("✅ Data/Hora: " + visitante.getDataHora());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Entrada registrada com sucesso");
            response.put("documento", visitante.getDocumento());
            response.put("nome", visitante.getNome());
            response.put("sobrenome", visitante.getSobrenome());
            response.put("numeroVisitas", visitante.getNumeroVisitas());
            response.put("dataHora", visitante.getDataHora());
            response.put("destino", registro.getSetor() + " / " + registro.getUnidade());

            System.out.println("✅ Retornando resposta de sucesso");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("💥 ========== ERRO CRÍTICO ==========");
            System.err.println("💥 Erro ao registrar entrada: " + e.getMessage());
            System.err.println("💥 Tipo do erro: " + e.getClass().getName());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro ao registrar entrada: " + e.getMessage()));
        } finally {
            System.out.println("🔵 ========== FIM DO REGISTRO ==========");
        }
    }

    // ============================================================
    // 🔍 BUSCAR HISTÓRICOS POR PERÍODO - CORRIGIDO
    // ============================================================
    @GetMapping("/historicos/por-periodo")
    public ResponseEntity<?> buscarHistoricosPorPeriodo(
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @RequestParam(required = false) String horaInicio,
            @RequestParam(required = false) String horaFim,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            // ✅ VALIDAÇÃO DAS DATAS
            if (dataInicio == null || dataFim == null) {
                return ResponseEntity.badRequest()
                        .body(criarRespostaErro("Data início e data fim são obrigatórias"));
            }

            // ✅ CONSTRUIR STRING NO FORMATO SQL SERVER
            String dataHoraInicioStr = dataInicio + " " + (horaInicio != null ? horaInicio : "00:00:00");
            String dataHoraFimStr = dataFim + " " + (horaFim != null ? horaFim : "23:59:59");

            System.out.println("🔍 Buscando históricos no período SQL: " + dataHoraInicioStr + " até " + dataHoraFimStr);

            // ✅ BUSCAR NO BANCO COM NATIVE QUERY CORRIGIDA
            List<Historico> historicos = historicoRepository.findByPeriodo(dataHoraInicioStr, dataHoraFimStr);

            // ✅ ENRIQUECER COM DADOS DOS VISITANTES E DESTINOS
            List<Map<String, Object>> historicosCompletos = new ArrayList<>();

            for (Historico historico : historicos) {
                Map<String, Object> historicoCompleto = new HashMap<>();
                historicoCompleto.put("historico", historico);

                // ✅ BUSCAR DADOS DO VISITANTE
                Optional<Visitante> visitante = visitanteService.buscarPorDocumento(historico.getDocumentoVisitante());
                if (visitante.isPresent()) {
                    historicoCompleto.put("visitante", visitante.get());
                } else {
                    // ✅ VISITANTE FALLBACK
                    Map<String, Object> visitanteFallback = new HashMap<>();
                    visitanteFallback.put("documento", historico.getDocumentoVisitante());
                    visitanteFallback.put("nome", "Visitante Não Cadastrado");
                    historicoCompleto.put("visitante", visitanteFallback);
                }

                // ✅ BUSCAR DESTINO
                List<HistoricoVisitado> destinos = historicoVisitadoRepository.findAllByCodigoHistorico(historico.getCodigo());
                if (!destinos.isEmpty()) {
                    HistoricoVisitado destino = destinos.get(0);
                    historicoCompleto.put("destino", destino);

                    String localCompleto = destino.getNomeSetorVisitado() + " / " + destino.getNomeUnidadeVisitado();
                    String pessoaVisitada = destino.getNomeVisitado() +
                            (destino.getSobrenomeVisitado() != null && !destino.getSobrenomeVisitado().isEmpty() ?
                                    " " + destino.getSobrenomeVisitado() : "");

                    historicoCompleto.put("localCompleto", localCompleto);
                    historicoCompleto.put("pessoaVisitada", pessoaVisitada);
                } else {
                    historicoCompleto.put("destino", null);
                    historicoCompleto.put("localCompleto", "Destino Não Especificado");
                    historicoCompleto.put("pessoaVisitada", "Sistema");
                }

                historicosCompletos.add(historicoCompleto);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dataInicio", dataHoraInicioStr);
            response.put("dataFim", dataHoraFimStr);
            response.put("totalRegistros", historicosCompletos.size());
            response.put("historicos", historicosCompletos);

            System.out.println("✅ " + historicosCompletos.size() + " registros encontrados no período");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("💥 ERRO na busca por período: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro na busca por período: " + e.getMessage()));
        }
    }

    @GetMapping("/visitantes/por-empresa/{empresa}")
    public ResponseEntity<?> buscarPorEmpresa(@PathVariable String empresa,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            List<Visitante> visitantes = visitanteRepository.findByEmpresaContaining(empresa.toUpperCase());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quantidade", visitantes.size());
            response.put("empresaBuscada", empresa);
            response.put("resultados", visitantes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro na busca por empresa: " + e.getMessage()));
        }
    }

    // ============================================================
    // 🔍 BUSCAR VISITANTE (EXATO)
    // ============================================================
    @GetMapping("/visitantes/{documento}")
    public ResponseEntity<?> buscarVisitante(@PathVariable String documento,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            Optional<Visitante> visitante = visitanteService.buscarPorDocumento(documento);

            if (visitante.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("visitante", visitante.get());

                List<Historico> historicos =
                        historicoRepository.findByDocumentoVisitanteOrderByDataHoraEntradaDesc(visitante.get().getDocumento());

                response.put("totalVisitas", historicos.size());
                response.put("ultimasVisitas", historicos.stream().limit(50).toList());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(criarRespostaErro("Visitante não encontrado"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro na busca: " + e.getMessage()));
        }
    }

    // ============================================================
    // 🔍 BUSCAR VISITANTES (PARCIAL)
    // ============================================================
    @GetMapping("/visitantes/busca/{termo}")
    public ResponseEntity<?> buscarVisitantesParcial(@PathVariable String termo,
                                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            List<Visitante> visitantes = visitanteRepository.findByDocumentoContaining(termo.toUpperCase());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quantidade", visitantes.size());
            response.put("termoBuscado", termo);
            response.put("resultados", visitantes);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro na busca: " + e.getMessage()));
        }
    }

    // ============================================================
    // 🔍 BUSCA INTELIGENTE (EXATA OU PARCIAL)
    // ============================================================
    @GetMapping("/visitantes/busca-inteligente/{termo}")
    public ResponseEntity<?> buscaInteligente(@PathVariable String termo,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            String termoNormalizado = termo.trim().toUpperCase().replace(" ", "");
            Map<String, Object> response = new HashMap<>();

            Optional<Visitante> visitanteExato = visitanteService.buscarPorDocumento(termoNormalizado);

            if (visitanteExato.isPresent()) {
                response.put("success", true);
                response.put("tipoBusca", "exata");
                response.put("quantidade", 1);
                response.put("resultados", List.of(visitanteExato.get()));
            } else {
                List<Visitante> visitantesParcial = visitanteRepository.findByDocumentoContaining(termoNormalizado);
                response.put("success", true);
                response.put("tipoBusca", "parcial");
                response.put("quantidade", visitantesParcial.size());
                response.put("resultados", visitantesParcial);

                if (visitantesParcial.isEmpty()) {
                    response.put("mensagem", "Nenhum visitante encontrado com: " + termo);
                }
            }

            response.put("termoBuscado", termo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro na busca inteligente: " + e.getMessage()));
        }
    }

    // ============================================================
    // 🧾 HISTÓRICO DE UM VISITANTE
    // ============================================================
    @GetMapping("/visitantes/{documento}/historico")
    public ResponseEntity<?> historicoVisitante(@PathVariable String documento,
                                                @RequestParam(defaultValue = "50") int limit,
                                                @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            String doc = documento.trim().toUpperCase().replace(" ", "");
            Optional<Visitante> visitante = visitanteService.buscarPorDocumento(doc);
            if (visitante.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(criarRespostaErro("Visitante não encontrado"));
            }

            List<Historico> historicos =
                    historicoRepository.findByDocumentoVisitanteOrderByDataHoraEntradaDesc(doc);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documento", doc);
            response.put("nome", visitante.get().getNome());
            response.put("totalVisitas", historicos.size());
            response.put("limite", limit);
            response.put("historicos", historicos.stream().limit(limit).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro ao buscar histórico: " + e.getMessage()));
        }
    }

    // ============================================================
    // 🔍 HISTÓRICO COMPLETO COM DESTINOS - NOVO ENDPOINT
    // ============================================================
    @GetMapping("/visitantes/{documento}/historico-completo")
    public ResponseEntity<?> historicoVisitanteCompleto(@PathVariable String documento,
                                                        @RequestParam(defaultValue = "5000") int limit,  // ✅ LIMITE 5000
                                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            String doc = documento.trim().toUpperCase().replace(" ", "");
            Optional<Visitante> visitante = visitanteService.buscarPorDocumento(doc);

            if (visitante.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(criarRespostaErro("Visitante não encontrado"));
            }

            List<Historico> historicos =
                    historicoRepository.findByDocumentoVisitanteOrderByDataHoraEntradaDesc(doc);

            // ✅ LISTA COM LIMITE DE 5000
            List<Map<String, Object>> historicosCompletos = new ArrayList<>();

            for (Historico historico : historicos) {
                if (historicosCompletos.size() >= limit) {
                    System.out.println("ℹ️ Limite de " + limit + " registros atingido para: " + doc);
                    break;
                }

                Map<String, Object> historicoCompleto = new HashMap<>();
                historicoCompleto.put("historico", historico);

                // ✅ BUSCAR DESTINO (já corrigido para múltiplos resultados)
                List<HistoricoVisitado> destinos = historicoVisitadoRepository.findAllByCodigoHistorico(historico.getCodigo());

                if (!destinos.isEmpty()) {
                    HistoricoVisitado destino = destinos.get(0);
                    historicoCompleto.put("destino", destino);

                    String localCompleto = destino.getNomeSetorVisitado() + " / " + destino.getNomeUnidadeVisitado();
                    String pessoaVisitada = destino.getNomeVisitado() +
                            (destino.getSobrenomeVisitado() != null && !destino.getSobrenomeVisitado().isEmpty() ?
                                    " " + destino.getSobrenomeVisitado() : "");

                    historicoCompleto.put("localCompleto", localCompleto);
                    historicoCompleto.put("pessoaVisitada", pessoaVisitada);
                } else {
                    historicoCompleto.put("destino", null);
                    historicoCompleto.put("localCompleto", "Destino Não Especificado");
                    historicoCompleto.put("pessoaVisitada", "Sistema");
                }

                historicosCompletos.add(historicoCompleto);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documento", doc);
            response.put("nome", visitante.get().getNome());
            response.put("totalVisitas", historicos.size());
            response.put("limiteAplicado", limit);
            response.put("historicosRetornados", historicosCompletos.size());
            response.put("historicos", historicosCompletos);

            System.out.println("🎯 HISTÓRICO: " + historicosCompletos.size() + " de " + historicos.size() + " (limite: " + limit + ")");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("💥 ERRO no histórico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro ao buscar histórico: " + e.getMessage()));
        }
    }

    // ============================================================
// 🏠 BUSCAR ENDEREÇOS POR NÚMERO - NOVO ENDPOINT
// ============================================================
    @GetMapping("/historicos-visitados/por-numero")
    public ResponseEntity<?> buscarEnderecosPorNumero(
            @RequestParam String numero,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("🔍 ========== BUSCA ENDEREÇOS POR NÚMERO ==========");
        System.out.println("🔍 Número recebido: " + numero);

        // 🔒 Validar token
        String token = extrairToken(authHeader);
        if (!validarToken(token)) {
            System.out.println("❌ Token inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            // ✅ Normalizar o número (remover espaços, converter para maiúsculas)
            String numeroNormalizado = numero.trim().toUpperCase();
            System.out.println("🔍 Número normalizado: " + numeroNormalizado);

            // ✅ BUSCAR ENDEREÇOS POR NÚMERO NO SETOR
            List<HistoricoVisitado> enderecosEncontrados = new ArrayList<>();

            // Busca 1: Por nome do setor (número)
            List<HistoricoVisitado> porSetor = historicoVisitadoRepository.findByNomeSetorVisitadoContaining(numeroNormalizado);
            System.out.println("📍 Encontrados por setor: " + porSetor.size());

            if (!porSetor.isEmpty()) {
                enderecosEncontrados.addAll(porSetor);
            }

            // Busca 2: Por nome da unidade (rua)
            List<HistoricoVisitado> porUnidade = historicoVisitadoRepository.findByNomeUnidadeVisitadoContaining(numeroNormalizado);
            System.out.println("📍 Encontrados por unidade: " + porUnidade.size());

            if (!porUnidade.isEmpty()) {
                enderecosEncontrados.addAll(porUnidade);
            }

            // Busca 3: Por nome do visitado (proprietário)
            List<HistoricoVisitado> porVisitado = historicoVisitadoRepository.findByNomeVisitadoContaining(numeroNormalizado);
            System.out.println("📍 Encontrados por visitado: " + porVisitado.size());

            if (!porVisitado.isEmpty()) {
                enderecosEncontrados.addAll(porVisitado);
            }

            // ✅ REMOVER DUPLICADOS (mesmo setor + mesma unidade)
            List<HistoricoVisitado> enderecosUnicos = enderecosEncontrados.stream()
                    .filter(endereco -> endereco.getNomeSetorVisitado() != null && endereco.getNomeUnidadeVisitado() != null)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(
                                    Comparator.comparing((HistoricoVisitado e) ->
                                            e.getNomeSetorVisitado() + "|" + e.getNomeUnidadeVisitado()
                                    )
                            )),
                            ArrayList::new
                    ));

            System.out.println("📍 Endereços únicos encontrados: " + enderecosUnicos.size());

            if (!enderecosUnicos.isEmpty()) {
                // ✅ FORMATAR RESPOSTA
                List<Map<String, Object>> enderecosFormatados = enderecosUnicos.stream()
                        .map(endereco -> {
                            Map<String, Object> enderecoMap = new HashMap<>();
                            enderecoMap.put("nomeSetorVisitado", endereco.getNomeSetorVisitado());
                            enderecoMap.put("nomeUnidadeVisitado", endereco.getNomeUnidadeVisitado());
                            enderecoMap.put("nomeVisitado", endereco.getNomeVisitado());
                            enderecoMap.put("sobrenomeVisitado", endereco.getSobrenomeVisitado());
                            enderecoMap.put("localCompleto", endereco.getNomeSetorVisitado() + " / " + endereco.getNomeUnidadeVisitado());
                            enderecoMap.put("pessoaVisitada", endereco.getNomeVisitado() +
                                    (endereco.getSobrenomeVisitado() != null ? " " + endereco.getSobrenomeVisitado() : ""));
                            return enderecoMap;
                        })
                        .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("quantidade", enderecosUnicos.size());
                response.put("numeroBuscado", numero);
                response.put("enderecos", enderecosFormatados);

                System.out.println("✅ Busca concluída: " + enderecosUnicos.size() + " endereço(s) encontrado(s)");
                return ResponseEntity.ok(response);

            } else {
                System.out.println("ℹ️ Nenhum endereço encontrado para: " + numero);
                return ResponseEntity.ok(criarRespostaSucesso(
                        "Nenhum endereço encontrado",
                        Collections.emptyList(),
                        0,
                        numero
                ));
            }

        } catch (Exception e) {
            System.err.println("💥 ERRO na busca de endereços: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro na busca de endereços: " + e.getMessage()));
        } finally {
            System.out.println("🔍 ========== FIM DA BUSCA ==========");
        }
    }

    // ✅ MÉTODO AUXILIAR PARA RESPOSTA DE SUCESSO
    private Map<String, Object> criarRespostaSucesso(String mensagem, List<?> dados, int quantidade, String numeroBuscado) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", mensagem);
        response.put("enderecos", dados);
        response.put("quantidade", quantidade);
        response.put("numeroBuscado", numeroBuscado);
        return response;
    }

    // ============================================================
    // 📊 ESTATÍSTICAS GERAIS
    // ============================================================
    @GetMapping("/estatisticas")
    public ResponseEntity<?> estatisticas(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            long totalVisitantes = visitanteRepository.count();
            long visitasHoje = historicoRepository.countTodayEntries();
            long totalEntradas = historicoRepository.count();

            Map<String, Object> estatisticas = new HashMap<>();
            estatisticas.put("success", true);
            estatisticas.put("totalVisitantes", totalVisitantes);
            estatisticas.put("visitasHoje", visitasHoje);
            estatisticas.put("totalEntradas", totalEntradas);
            estatisticas.put("dataConsulta", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(estatisticas);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro ao obter estatísticas: " + e.getMessage()));
        }
    }

    // ============================================================
    // 🧾 LISTAR TODOS OS VISITANTES (SIMPLES E FUNCIONAL)
    // ============================================================
    @GetMapping("/visitantes")
    public ResponseEntity<?> listarVisitantes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            // ✅ PAGINAÇÃO IMPLEMENTADA
            List<Visitante> visitantes = visitanteRepository.findWithPagination(page * size, size);
            long totalRegistros = visitanteRepository.count();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paginaAtual", page);
            response.put("tamanhoPagina", size);
            response.put("totalRegistros", totalRegistros);
            response.put("totalPaginas", (int) Math.ceil((double) totalRegistros / size));
            response.put("quantidade", visitantes.size());
            response.put("visitantes", visitantes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro ao listar visitantes: " + e.getMessage()));
        }
    }

    // 🧾 LISTAR TODOS OS HISTÓRICOS (COM PAGINAÇÃO)
    @GetMapping("/historicos")
    public ResponseEntity<?> listarHistoricos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false, defaultValue = "false") boolean todos,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!validarToken(extrairToken(authHeader))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(criarRespostaErro("Token inválido ou expirado"));
        }

        try {
            List<Historico> historicos;
            long totalRegistros = historicoRepository.count();

            if (todos) {
                // ✅ Caso o app queira TUDO (sem paginação)
                historicos = historicoRepository.findAllOrderByDataHoraEntradaDesc();
            } else {
                // ✅ Paginação normal
                historicos = historicoRepository.findWithPagination(page * size, size);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("todos", todos);
            response.put("paginaAtual", page);
            response.put("tamanhoPagina", size);
            response.put("totalRegistros", totalRegistros);
            response.put("totalPaginas", (int) Math.ceil((double) totalRegistros / size));
            response.put("quantidade", historicos.size());
            response.put("historicos", historicos);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarRespostaErro("Erro ao listar históricos: " + e.getMessage()));
        }
    }

    // ============================================================
    // ⚠️ MÉTODO AUXILIAR DE ERRO
    // ============================================================
    private Map<String, Object> criarRespostaErro(String mensagem) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", mensagem);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
}