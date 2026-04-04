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
public class SimulacionRequest {
    private List<ItemSimulacion> simulaciones;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemSimulacion {
        private Integer ponderacionId;
        private Integer nota;
    }
}
