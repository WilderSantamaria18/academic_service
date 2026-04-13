package com.idat.pe.academic_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumenAcademicoResponse {
    private String nombreAsignatura;
    private Double promedioActual;
    private String porcentajeRestante; // Ej: "40%"
    private Double notaNecesariaRestante; // Para llegar a 13 (12.5)
    private String estado; // PENDIENTE, APROBADA, DESAPROBADA
    private List<DetalleResumenDTO> detalles;
    private String mensajeInformativo;
}
