package com.idat.pe.academic_service.service;

import com.idat.pe.academic_service.client.UsuarioClient;
import com.idat.pe.academic_service.dto.AsignaturaRequest;
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

@Service
@RequiredArgsConstructor
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final UsuarioClient usuarioClient;
    private final JwtService jwtService;
    private final HttpServletRequest request;

    @Transactional
    public Asignatura crearAsignatura(AsignaturaRequest requestDto) {
        String token = authHeaderConBearer();
        Integer usuarioId = jwtService.extractUserId(token.substring(7));
        
        // VALIDACIÓN: Verificar si el usuario existe en el auth_service vía Feign
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
        return asignaturaRepository.save(asignatura);
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
        return input.stream()
                .map(String::valueOf)
                .map(BigDecimal::new)
                .toList();
    }

    private void validarSumaCienPorCiento(List<BigDecimal> porcentajes) {
        BigDecimal suma = porcentajes.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (suma.compareTo(BigDecimal.ONE) != 0) {
            throw new RuntimeException("La suma de las ponderaciones debe ser exactamente 1.00 (100%)");
        }
    }

    public List<Asignatura> listarMisAsignaturas() {
        return asignaturaRepository.findByUsuarioId(jwtService.extractUserId(authHeaderConBearer().substring(7)));
    }

    private String authHeaderConBearer() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader;
        }
        throw new RuntimeException("Token no encontrado o inválido en el encabezado Authorization");
    }
}
