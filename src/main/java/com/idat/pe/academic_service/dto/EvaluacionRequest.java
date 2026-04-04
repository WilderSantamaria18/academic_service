package com.idat.pe.academic_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionRequest {

    @NotNull(message = "El ID de la asignatura es obligatorio")
    private Integer asignaturaId;

    @NotNull(message = "El ID de la ponderación es obligatorio")
    private Integer ponderacionId;

    @NotBlank(message = "El nombre de la evaluación es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @NotNull(message = "La nota es obligatoria")
    @Min(value = 0, message = "La nota mínima es 0")
    @Max(value = 20, message = "La nota máxima es 20")
    private Integer nota; // HU-05: Solo valores enteros
}
