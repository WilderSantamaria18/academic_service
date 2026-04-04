package com.idat.pe.academic_service.controller;

import com.idat.pe.academic_service.dto.ResumenAcademicoResponse;
import com.idat.pe.academic_service.dto.SimulacionRequest;
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

    @PostMapping("/{id}/simular")
    public ResponseEntity<ResumenAcademicoResponse> simular(
            @PathVariable Integer id,
            @RequestBody SimulacionRequest request) {
        return ResponseEntity.ok(academicoService.simularNotas(id, request));
    }
}
