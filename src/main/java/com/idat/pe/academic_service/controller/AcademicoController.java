package com.idat.pe.academic_service.controller;

import com.idat.pe.academic_service.dto.ResumenAcademicoResponse;
import com.idat.pe.academic_service.service.AcademicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asignaturas")
@RequiredArgsConstructor
public class AcademicoController {

    private final AcademicoService academicoService;

    @GetMapping("/{id}/resumen")
    public ResponseEntity<ResumenAcademicoResponse> obtenerResumen(@PathVariable Integer id) {
        return ResponseEntity.ok(academicoService.obtenerResumen(id));
    }

    // Nuevo endpoint GET para simular/calcular la nota restante de manera automática
    @GetMapping("/{id}/cuanto-me-falta")
    public ResponseEntity<ResumenAcademicoResponse> cuantoMeFalta(@PathVariable Integer id) {
        return ResponseEntity.ok(academicoService.calcularNotaFaltante(id));
    }
}
