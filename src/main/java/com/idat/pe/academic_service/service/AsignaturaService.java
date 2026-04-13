package com.idat.pe.academic_service.service;

import com.idat.pe.academic_service.remote.client.UsuarioClient;
import com.idat.pe.academic_service.dto.AsignaturaRequest;
import com.idat.pe.academic_service.dto.AsignaturaResponse;
import com.idat.pe.academic_service.dto.PonderacionResponse;
import com.idat.pe.academic_service.entity.Asignatura;
import com.idat.pe.academic_service.entity.Ponderacion;
import com.idat.pe.academic_service.repository.AsignaturaRepository;
import com.idat.pe.academic_service.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final UsuarioClient usuarioClient;
    private final JwtService jwtService;
    private final HttpServletRequest request;

    @Transactional
    public AsignaturaResponse crearAsignatura(AsignaturaRequest requestDto) {
        String token = authHeaderConBearer();
        Integer usuarioId = jwtService.extractUserId(token.substring(7));
        
        try {
            usuarioClient.obtenerUsuario(usuarioId, token);
        } catch (Exception e) {
            throw new RuntimeException("Usuario no encontrado o error de comunicación con auth_service");
        }
        
        Asignatura asignatura = Asignatura.builder()
                .nombre(requestDto.getNombre())
                .usuarioId(usuarioId)
                .build();

        List<BigDecimal> porcentajes = configurarPorcentajes(requestDto.getPonderaciones());
        validarSumaCienPorCiento(porcentajes);

        List<Ponderacion> ponderaciones = new ArrayList<>();
        for (int i = 0; i < porcentajes.size(); i++) {
            Ponderacion p = Ponderacion.builder()
                    .asignatura(asignatura)
                    .porcentaje(porcentajes.get(i))
                    .orden(i + 1)
                    .build();
            ponderaciones.add(p);
        }
        
        asignatura.setPonderaciones(ponderaciones);
        Asignatura guardada = asignaturaRepository.save(asignatura);
        return mapToResponse(guardada);
    }

    private List<BigDecimal> configurarPorcentajes(List<Double> input) {
        if (input == null || input.isEmpty()) {
            return Arrays.asList(
                    new BigDecimal("0.04"),
                    new BigDecimal("0.12"),
                    new BigDecimal("0.24"),
                    new BigDecimal("0.60")
            );
        }
        return input.stream().map(String::valueOf).map(BigDecimal::new).toList();
    }

    private void validarSumaCienPorCiento(List<BigDecimal> porcentajes) {
        BigDecimal suma = porcentajes.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (suma.compareTo(BigDecimal.ONE) != 0) {
            throw new RuntimeException("La suma de las ponderaciones debe ser exactamente 1.00 (100%)");
        }
    }

    public List<AsignaturaResponse> listarMisAsignaturas() {
        Integer usuarioId = jwtService.extractUserId(authHeaderConBearer().substring(7));
        return asignaturaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AsignaturaResponse actualizarAsignatura(Integer id, AsignaturaRequest requestDto) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        Integer usuarioId = jwtService.extractUserId(authHeaderConBearer().substring(7));
        if (!asignatura.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tiene permisos para actualizar esta asignatura");
        }

        asignatura.setNombre(requestDto.getNombre());

        if (requestDto.getPonderaciones() != null && !requestDto.getPonderaciones().isEmpty()) {
            List<BigDecimal> porcentajes = configurarPorcentajes(requestDto.getPonderaciones());
            validarSumaCienPorCiento(porcentajes);

            List<Ponderacion> actuales = asignatura.getPonderaciones();

            // Si la cantidad de ponderaciones es la misma, actualizamos los valores existentes
            // Esto evita errores de 'Duplicate entry' y mantiene la relación con Evaluacion
            if (actuales.size() == porcentajes.size()) {
                for (int i = 0; i < actuales.size(); i++) {
                    actuales.get(i).setPorcentaje(porcentajes.get(i));
                }
            } else {
                // Solo si la cantidad cambia, limpiamos y recreamos (caso poco probable)
                actuales.clear();
                asignaturaRepository.saveAndFlush(asignatura);

                for (int i = 0; i < porcentajes.size(); i++) {
                    Ponderacion p = Ponderacion.builder()
                            .asignatura(asignatura)
                            .porcentaje(porcentajes.get(i))
                            .orden(i + 1)
                            .build();
                    actuales.add(p);
                }
            }
        }

        Asignatura actualizada = asignaturaRepository.save(asignatura);
        return mapToResponse(actualizada);
    }

    @Transactional
    public void eliminarAsignatura(Integer id) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        Integer usuarioId = jwtService.extractUserId(authHeaderConBearer().substring(7));
        if (!asignatura.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tiene permisos para eliminar esta asignatura");
        }

        asignaturaRepository.delete(asignatura);
    }

    private String authHeaderConBearer() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader;
        }
        throw new RuntimeException("Token no encontrado o inválido");
    }

    private AsignaturaResponse mapToResponse(Asignatura a) {
        return AsignaturaResponse.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .usuarioId(a.getUsuarioId())
                .ponderaciones(a.getPonderaciones().stream()
                        .map(p -> PonderacionResponse.builder()
                                .id(p.getId())
                                .porcentaje(p.getPorcentaje())
                                .orden(p.getOrden())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
