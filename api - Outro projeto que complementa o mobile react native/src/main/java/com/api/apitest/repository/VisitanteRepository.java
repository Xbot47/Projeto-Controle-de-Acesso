package com.api.apitest.repository;

import com.api.apitest.entities.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Long> {

    // ✅ CORREÇÃO: Buscar visitante por documento exato
    Optional<Visitante> findByDocumento(String documento);

    // ✅ CORREÇÃO: Buscar visitantes por nome (parcial)
    List<Visitante> findByNomeContaining(String nome);

    // ✅ CORREÇÃO: Buscar visitantes por parte do documento
    List<Visitante> findByDocumentoContaining(String documento);

    // ✅ CORREÇÃO: Buscar visitantes por nome da empresa
    List<Visitante> findByEmpresaContaining(String empresa);

    // ✅ CORREÇÃO: Busca inteligente em múltiplos campos
    @Query("SELECT v FROM Visitante v WHERE " +
            "v.documento LIKE %:termo% OR " +
            "v.nome LIKE %:termo% OR " +
            "v.empresa LIKE %:termo% OR " +
            "v.sobrenome LIKE %:termo%")
    List<Visitante> buscaInteligente(@Param("termo") String termo);

    // Contar total de visitantes
    long count();

    // ✅ CORREÇÃO CRÍTICA: Tabela correta "Visitantes"
    @Query(value = "SELECT * FROM Visitantes ORDER BY DataHora DESC OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY",
            nativeQuery = true)
    List<Visitante> findWithPagination(@Param("offset") int offset, @Param("limit") int limit);
}