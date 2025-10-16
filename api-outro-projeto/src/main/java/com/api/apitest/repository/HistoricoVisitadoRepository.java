package com.api.apitest.repository;

import com.api.apitest.entities.HistoricoVisitado;
import com.api.apitest.entities.HistoricoVisitadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoVisitadoRepository extends JpaRepository<HistoricoVisitado, HistoricoVisitadoId> {

    // ✅ MÉTODO CORRIGIDO: Buscar por código do histórico
    @Query(value = "SELECT * FROM HistoricosVisitados WHERE Codigo_Historicos = :codigoHistorico", nativeQuery = true)
    List<HistoricoVisitado> findAllByCodigoHistorico(Long codigoHistorico);

    // ✅ NOVOS MÉTODOS PARA BUSCA POR NÚMERO
    @Query("SELECT h FROM HistoricoVisitado h WHERE UPPER(h.nomeSetorVisitado) LIKE UPPER(CONCAT('%', :numero, '%'))")
    List<HistoricoVisitado> findByNomeSetorVisitadoContaining(@Param("numero") String numero);

    @Query("SELECT h FROM HistoricoVisitado h WHERE UPPER(h.nomeUnidadeVisitado) LIKE UPPER(CONCAT('%', :numero, '%'))")
    List<HistoricoVisitado> findByNomeUnidadeVisitadoContaining(@Param("numero") String numero);

    @Query("SELECT h FROM HistoricoVisitado h WHERE UPPER(h.nomeVisitado) LIKE UPPER(CONCAT('%', :numero, '%'))")
    List<HistoricoVisitado> findByNomeVisitadoContaining(@Param("numero") String numero);

    // ✅ BUSCA MAIS AMPLA (OPCIONAL)
    @Query("SELECT h FROM HistoricoVisitado h WHERE " +
            "UPPER(h.nomeSetorVisitado) LIKE UPPER(CONCAT('%', :termo, '%')) OR " +
            "UPPER(h.nomeUnidadeVisitado) LIKE UPPER(CONCAT('%', :termo, '%')) OR " +
            "UPPER(h.nomeVisitado) LIKE UPPER(CONCAT('%', :termo, '%'))")
    List<HistoricoVisitado> findByAnyFieldContaining(@Param("termo") String termo);
}