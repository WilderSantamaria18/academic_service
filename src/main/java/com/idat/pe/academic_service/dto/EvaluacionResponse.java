package com.idat.pe.academic_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionResponse {
    private Integer id;
    private String nombre;
    private Integer nota;
    private Integer ponderacionId;
    private String mensaje; // HU-05: Estado (Creada, Actualizada, Eliminada)
}
