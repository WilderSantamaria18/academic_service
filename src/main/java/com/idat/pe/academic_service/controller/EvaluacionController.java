package com.idat.pe.academic_service.controller;

import com.idat.pe.academic_service.dto.EvaluacionRequest;
import com.idat.pe.academic_service.dto.EvaluacionResponse;
import com.idat.pe.academic_service.service.EvaluacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluaciones")
@RequiredArgsConstructor
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    @PostMapping
    public ResponseEntity<EvaluacionResponse> crear(@Valid @RequestBody EvaluacionRequest request) {
        return new ResponseEntity<>(evaluacionService.crearEvaluacion(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvaluacionResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EvaluacionRequest request) {
        return ResponseEntity.ok(evaluacionService.actualizarEvaluacion(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EvaluacionResponse> eliminar(@PathVariable Integer id) {
        evaluacionService.eliminarEvaluacion(id);
        
        EvaluacionResponse response = EvaluacionResponse.builder()
                .mensaje("Evaluación eliminada correctamente (Registro borrado)")
                .build();
        
        return ResponseEntity.ok(response);
    }
}
