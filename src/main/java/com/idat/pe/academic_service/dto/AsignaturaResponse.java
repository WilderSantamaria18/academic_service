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
public class AsignaturaResponse {
    private Integer id;
    private String nombre;
    private Integer usuarioId;
    private List<PonderacionResponse> ponderaciones;
}
