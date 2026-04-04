package com.idat.pe.academic_service.controller;

import com.idat.pe.academic_service.dto.AsignaturaRequest;
import com.idat.pe.academic_service.dto.AsignaturaResponse;
import com.idat.pe.academic_service.service.AsignaturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaturas")
@RequiredArgsConstructor
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    @PostMapping
    public ResponseEntity<AsignaturaResponse> crear(@Valid @RequestBody AsignaturaRequest request) {
        return new ResponseEntity<>(asignaturaService.crearAsignatura(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AsignaturaResponse>> listar() {
        return ResponseEntity.ok(asignaturaService.listarMisAsignaturas());
    }
}
