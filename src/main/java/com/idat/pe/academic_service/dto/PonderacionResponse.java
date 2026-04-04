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
public class PonderacionResponse {
    private Integer id;
    private BigDecimal porcentaje;
    private Integer orden;
}
