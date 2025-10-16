package com.api.apitest.services;

import com.api.apitest.entities.Historico;
import com.api.apitest.repository.HistoricoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository historicoRepository;

    // ✅ Buscar todos os históricos (sem limite)
    public List<Historico> listarTodos() {
        return historicoRepository.findAllOrderByDataHoraEntradaDesc();
    }

    // ✅ Buscar com limite (opcional)
    public List<Historico> listarComLimite(Integer limit) {
        if (limit == null || limit <= 0) {
            return listarTodos(); // se não passar limite, traz todos
        }
        return historicoRepository.findTopNByOrderByDataHoraEntradaDesc(limit);
    }

    // ✅ Buscar com paginação (offset e limit)
    public List<Historico> listarPaginado(int offset, int limit) {
        return historicoRepository.findWithPagination(offset, limit);
    }

    // ✅ Contar total
    public long contarTotal() {
        return historicoRepository.countTotal();
    }
}
