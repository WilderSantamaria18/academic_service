package com.idat.pe.academic_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AsignaturaRequest {

    @NotBlank(message = "El nombre de la asignatura es requerido")
    private String nombre;

    // Lista de porcentajes (ej: [0.04, 0.12, 0.24, 0.60])
    // Si viene nulo o vacío, usaremos los valores por defecto de la HU-04
    private List<Double> ponderaciones;
}
