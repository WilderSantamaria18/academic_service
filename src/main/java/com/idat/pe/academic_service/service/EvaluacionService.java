package com.idat.pe.academic_service.service;

import com.idat.pe.academic_service.dto.EvaluacionRequest;
import com.idat.pe.academic_service.dto.EvaluacionResponse;
import com.idat.pe.academic_service.entity.Asignatura;
import com.idat.pe.academic_service.entity.Evaluacion;
import com.idat.pe.academic_service.entity.Ponderacion;
import com.idat.pe.academic_service.repository.AsignaturaRepository;
import com.idat.pe.academic_service.repository.EvaluacionRepository;
import com.idat.pe.academic_service.repository.PonderacionRepository;
import com.idat.pe.academic_service.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final PonderacionRepository ponderacionRepository;
    private final JwtService jwtService;
    private final HttpServletRequest request;

    @Transactional
    public EvaluacionResponse crearEvaluacion(EvaluacionRequest dto) {
        String token = authHeaderConBearer();
        Integer usuarioId = jwtService.extractUserId(token.substring(7));

        // 1. Validar Asignatura y Propiedad
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        
        if (!asignatura.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso. Dueño asignatura: " + asignatura.getUsuarioId() + ", Usuario en Token: " + usuarioId);
        }

        // 2. Validar Ponderación y Relación con Asignatura
        Ponderacion ponderacion = ponderacionRepository.findById(dto.getPonderacionId())
                .orElseThrow(() -> new RuntimeException("Ponderación no encontrada"));
        
        if (!ponderacion.getAsignatura().getId().equals(asignatura.getId())) {
            throw new RuntimeException("La ponderación elegida no pertenece a la asignatura seleccionada");
        }

        // 3. Validar duplicidad (Sin sustitución automática en POST)
        if (evaluacionRepository.existsByPonderacionId(dto.getPonderacionId())) {
            throw new RuntimeException("Ya existe una nota registrada para esta ponderación. Use la opción de actualizar.");
        }

        // 4. Guardar
        Evaluacion evaluacion = Evaluacion.builder()
                .asignatura(asignatura)
                .ponderacion(ponderacion)
                .nombre(dto.getNombre())
                .nota(dto.getNota()) // Nota ya validada por @Min/@Max en DTO
                .build();

        Evaluacion guardada = evaluacionRepository.save(evaluacion);

        return mapToResponse(guardada, "Evaluación creada correctamente (Nuevo registro)");
    }

    @Transactional
    public EvaluacionResponse actualizarEvaluacion(Integer id, EvaluacionRequest dto) {
        String token = authHeaderConBearer();
        Integer usuarioId = jwtService.extractUserId(token.substring(7));

        Evaluacion evaluacion = evaluacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada"));

        // Validar propiedad
        if (!evaluacion.getAsignatura().getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para modificar esta evaluación");
        }

        // Actualizar datos
        evaluacion.setNombre(dto.getNombre());
        evaluacion.setNota(dto.getNota());

        Evaluacion actualizada = evaluacionRepository.save(evaluacion);
        return mapToResponse(actualizada, "Evaluación actualizada correctamente");
    }

    @Transactional
    public void eliminarEvaluacion(Integer id) {
        String token = authHeaderConBearer();
        Integer usuarioId = jwtService.extractUserId(token.substring(7));

        Evaluacion evaluacion = evaluacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada"));

        if (!evaluacion.getAsignatura().getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta evaluación");
        }

        evaluacionRepository.delete(evaluacion);
    }

    private String authHeaderConBearer() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader;
        }
        throw new RuntimeException("Token no encontrado o inválido");
    }

    private EvaluacionResponse mapToResponse(Evaluacion e, String mensaje) {
        return EvaluacionResponse.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .nota(e.getNota())
                .ponderacionId(e.getPonderacion().getId())
                .mensaje(mensaje)
                .build();
    }
}
