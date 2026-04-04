package com.idat.pe.academic_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleResumenDTO {
    private Integer ponderacionId;
    private Integer orden;
    private BigDecimal porcentaje;
    private Integer nota; // null si no está registrada
    private Double contribucionAlPromedio; // nota * porcentaje
}
