package com.api.apitest.repository;

import com.api.apitest.entities.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    // ✅ Buscar histórico por documento do visitante
    @Query(value = "SELECT * FROM Historicos WHERE DocumentoVisitante = :documentoVisitante ORDER BY DataHoraEntrada DESC", nativeQuery = true)
    List<Historico> findByDocumentoVisitanteOrderByDataHoraEntradaDesc(@Param("documentoVisitante") String documentoVisitante);

    // ✅ Busca com limite
    @Query(value = "SELECT TOP (:limit) * FROM Historicos WHERE DocumentoVisitante = :documentoVisitante ORDER BY DataHoraEntrada DESC", nativeQuery = true)
    List<Historico> findByDocumentoVisitanteWithLimit(@Param("documentoVisitante") String documentoVisitante,
                                                      @Param("limit") int limit);

    // ✅ Todos os históricos
    @Query(value = "SELECT * FROM Historicos ORDER BY DataHoraEntrada DESC", nativeQuery = true)
    List<Historico> findAllOrderByDataHoraEntradaDesc();

    // ✅ Históricos desde 2023
    @Query(value = "SELECT * FROM Historicos WHERE YEAR(DataHoraEntrada) >= 2023 ORDER BY DataHoraEntrada DESC", nativeQuery = true)
    List<Historico> findAllDesde2023();

    // ✅ Com limite
    @Query(value = "SELECT TOP (:limit) * FROM Historicos ORDER BY DataHoraEntrada DESC", nativeQuery = true)
    List<Historico> findTopNByOrderByDataHoraEntradaDesc(@Param("limit") int limit);

    // ✅ Contar visitas hoje
    @Query(value = "SELECT COUNT(*) FROM Historicos WHERE CAST(DataHoraEntrada AS DATE) = CAST(GETDATE() AS DATE)", nativeQuery = true)
    long countTodayEntries();

    // ✅ Visitas hoje
    @Query(value = "SELECT * FROM Historicos WHERE CAST(DataHoraEntrada AS DATE) = CAST(GETDATE() AS DATE) ORDER BY DataHoraEntrada DESC", nativeQuery = true)
    List<Historico> findTodayEntries();

    @Query(value = "SELECT * FROM Historicos ORDER BY DataHoraEntrada DESC OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY",
            nativeQuery = true)
    List<Historico> findWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    // ✅ CONTAGEM TOTAL
    @Query(value = "SELECT COUNT(*) FROM Historicos", nativeQuery = true)
    long countTotal();

    // ✅ BUSCAR HISTÓRICOS POR PERÍODO - CORRIGIDO PARA SQL SERVER
    @Query(value = """
        SELECT * FROM Historicos 
        WHERE DataHoraEntrada >= CONVERT(DATETIME, :dataInicio) 
          AND DataHoraEntrada <= CONVERT(DATETIME, :dataFim) 
        ORDER BY DataHoraEntrada DESC
        """, nativeQuery = true)
    List<Historico> findByPeriodo(@Param("dataInicio") String dataInicio,
                                  @Param("dataFim") String dataFim);

}