package com.api.apitest.repository;

import com.api.apitest.entities.CategoriasVisitantes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriasVisitantesRepository extends JpaRepository<CategoriasVisitantes, Integer> {

    // ✅ Buscar por nome exato
    @Query("SELECT c FROM CategoriasVisitantes c WHERE c.nome = :nome")
    Optional<CategoriasVisitantes> findByNome(@Param("nome") String nome);

    // ✅ Buscar todos ordenados
    List<CategoriasVisitantes> findByOrderByNome();

    // ✅ Verificar existência
    boolean existsById(Integer codigo);
}