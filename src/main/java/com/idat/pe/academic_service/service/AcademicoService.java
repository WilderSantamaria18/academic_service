package com.idat.pe.academic_service.service;

import com.idat.pe.academic_service.dto.DetalleResumenDTO;
import com.idat.pe.academic_service.dto.ResumenAcademicoResponse;
import com.idat.pe.academic_service.dto.SimulacionRequest;
import com.idat.pe.academic_service.entity.Asignatura;
import com.idat.pe.academic_service.entity.Ponderacion;
import com.idat.pe.academic_service.repository.AsignaturaRepository;
import com.idat.pe.academic_service.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicoService {

    private final AsignaturaRepository asignaturaRepository;
    private final JwtService jwtService;
    private final HttpServletRequest request;

    public ResumenAcademicoResponse obtenerResumen(Integer asignaturaId) {
        Asignatura asignatura = validarPropiedad(asignaturaId);
        
        List<DetalleResumenDTO> detalles = new ArrayList<>();
        double promedioActual = 0.0;
        double porcentajeCubierto = 0.0;

        for (Ponderacion p : asignatura.getPonderaciones()) {
            Integer nota = (p.getEvaluacion() != null) ? p.getEvaluacion().getNota() : null;
            double porcentajeVal = p.getPorcentaje().doubleValue();
            double contribucion = (nota != null) ? nota * porcentajeVal : 0.0;
            
            detalles.add(DetalleResumenDTO.builder()
                    .ponderacionId(p.getId())
                    .orden(p.getOrden())
                    .porcentaje(p.getPorcentaje())
                    .nota(nota)
                    .contribucionAlPromedio(contribucion)
                    .build());
            
            promedioActual += contribucion;
            if (nota != null) {
                porcentajeCubierto += porcentajeVal;
            }
        }

        double porcentajeRestante = 1.0 - porcentajeCubierto;
        double notaNecesaria = 0.0;
        String mensaje = "Sigue esforzándote";

        if (porcentajeRestante > 0) {
            notaNecesaria = (13.0 - promedioActual) / porcentajeRestante;
            if (notaNecesaria <= 0) {
                mensaje = "¡Felicidades! Ya aprobaste la asignatura.";
                notaNecesaria = 0.0;
            } else if (notaNecesaria > 20) {
                mensaje = "Situación crítica: Necesitas más de 20 para aprobar.";
            } else {
                mensaje = String.format("Necesitas un promedio de %.2f en lo que falta para aprobar con 13.", notaNecesaria);
            }
        } else {
            mensaje = (promedioActual >= 13) ? "Asignatura Aprobada" : "Asignatura Desaprobada";
        }

        return ResumenAcademicoResponse.builder()
                .nombreAsignatura(asignatura.getNombre())
                .promedioActual(Math.round(promedioActual * 100.0) / 100.0)
                .notaNecesariaRestante(Math.round(notaNecesaria * 100.0) / 100.0)
                .detalles(detalles)
                .mensajeInformativo(mensaje)
                .build();
    }

    public ResumenAcademicoResponse simularNotas(Integer asignaturaId, SimulacionRequest requestDto) {
        Asignatura asignatura = validarPropiedad(asignaturaId);
        
        // Mapa de ponderacionId -> nota simulada
        Map<Integer, Integer> notasSimuladas = requestDto.getSimulaciones().stream()
                .collect(Collectors.toMap(
                        SimulacionRequest.ItemSimulacion::getPonderacionId,
                        SimulacionRequest.ItemSimulacion::getNota
                ));

        List<DetalleResumenDTO> detalles = new ArrayList<>();
        double promedioProyectado = 0.0;

        for (Ponderacion p : asignatura.getPonderaciones()) {
            // Prioridad: 1. Nota simulada, 2. Nota real, 3. Cero
            Integer nota = notasSimuladas.get(p.getId());
            if (nota == null && p.getEvaluacion() != null) {
                nota = p.getEvaluacion().getNota();
            }
            
            double porcentajeVal = p.getPorcentaje().doubleValue();
            double contribucion = (nota != null) ? nota * porcentajeVal : 0.0;

            detalles.add(DetalleResumenDTO.builder()
                    .ponderacionId(p.getId())
                    .orden(p.getOrden())
                    .porcentaje(p.getPorcentaje())
                    .nota(nota)
                    .contribucionAlPromedio(contribucion)
                    .build());
            
            promedioProyectado += contribucion;
        }

        String mensaje = (promedioProyectado >= 13) 
                ? "Con estas notas APROBARÍAS la asignatura." 
                : "Aún con estas notas, no alcanzarías el 13 aprobatorio.";

        return ResumenAcademicoResponse.builder()
                .nombreAsignatura(asignatura.getNombre())
                .promedioActual(Math.round(promedioProyectado * 100.0) / 100.0)
                .detalles(detalles)
                .mensajeInformativo(mensaje)
                .build();
    }

    private Asignatura validarPropiedad(Integer asignaturaId) {
        String token = request.getHeader("Authorization");
        Integer usuarioId = jwtService.extractUserId(token.substring(7));

        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        
        if (!asignatura.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para ver esta asignatura");
        }
        return asignatura;
    }
}
