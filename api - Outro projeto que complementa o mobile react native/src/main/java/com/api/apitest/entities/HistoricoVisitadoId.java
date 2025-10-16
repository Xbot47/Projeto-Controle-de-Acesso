package com.api.apitest.entities;

import java.io.Serializable;
import java.util.Objects;

public class HistoricoVisitadoId implements Serializable {
    private String documentoVisitado;
    private Long codigoHistoricos;

    public HistoricoVisitadoId() {}

    public HistoricoVisitadoId(String documentoVisitado, Long codigoHistoricos) {
        this.documentoVisitado = documentoVisitado;
        this.codigoHistoricos = codigoHistoricos;
    }

    public String getDocumentoVisitado() { return documentoVisitado; }
    public void setDocumentoVisitado(String documentoVisitado) { this.documentoVisitado = documentoVisitado; }

    public Long getCodigoHistoricos() { return codigoHistoricos; }
    public void setCodigoHistoricos(Long codigoHistoricos) { this.codigoHistoricos = codigoHistoricos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoVisitadoId that = (HistoricoVisitadoId) o;
        return Objects.equals(documentoVisitado, that.documentoVisitado) &&
                Objects.equals(codigoHistoricos, that.codigoHistoricos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentoVisitado, codigoHistoricos);
    }
}