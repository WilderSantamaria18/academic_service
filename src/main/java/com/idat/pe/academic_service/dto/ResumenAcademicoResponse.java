package com.idat.pe.academic_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenAcademicoResponse {
    private String nombreAsignatura;
    private Double promedioActual;
    private Double notaNecesariaRestante; // Para llegar a 13
    private List<DetalleResumenDTO> detalles;
    private String mensajeInformativo;
}
