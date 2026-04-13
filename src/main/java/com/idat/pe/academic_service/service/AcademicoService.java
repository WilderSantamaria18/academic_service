package com.idat.pe.academic_service.service;

import com.idat.pe.academic_service.dto.DetalleResumenDTO;
import com.idat.pe.academic_service.dto.ResumenAcademicoResponse;
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

@Service
@RequiredArgsConstructor
public class AcademicoService {

    private final AsignaturaRepository asignaturaRepository;
    private final JwtService jwtService;
    private final HttpServletRequest request;
    private final com.idat.pe.academic_service.remote.client.UsuarioClient usuarioClient;

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

        if (porcentajeRestante > 0) {
            notaNecesaria = (12.5 - promedioActual) / porcentajeRestante; // Se necesita 12.5 para redondear a 13
            if (notaNecesaria <= 0) {
                notaNecesaria = 0.0;
            }
        }

        return ResumenAcademicoResponse.builder()
                .nombreAsignatura(asignatura.getNombre())
                .promedioActual(Math.round(promedioActual * 100.0) / 100.0)
                .notaNecesariaRestante(Math.round(notaNecesaria * 100.0) / 100.0)
                .detalles(detalles)
                .build();
    }

    // Nuevo método: "Calculadora de Aprobación" automatizada
    public ResumenAcademicoResponse calcularNotaFaltante(Integer asignaturaId) {
        Asignatura asignatura = validarPropiedad(asignaturaId);
        
        double promedioActualAcumulado = 0.0;
        double porcentajeCubierto = 0.0;
        int cantidadNotasRegistradas = 0;

        // 1. Leer automáticamente todas las notas registradas y sus porcentajes
        for (Ponderacion p : asignatura.getPonderaciones()) {
            if (p.getEvaluacion() != null && p.getEvaluacion().getNota() != null) {
                double porcentajeVal = p.getPorcentaje().doubleValue();
                promedioActualAcumulado += (p.getEvaluacion().getNota() * porcentajeVal);
                porcentajeCubierto += porcentajeVal;
                cantidadNotasRegistradas++;
            }
        }

        // 2. Analizar lo que falta
        double porcentajeRestanteDecimal = 1.0 - porcentajeCubierto;
        int porcentajeRestanteEntero = (int) Math.round(porcentajeRestanteDecimal * 100);
        String porcentajeRestanteStr = porcentajeRestanteEntero + "%";
        
        String estado;
        String mensaje;
        Double notaNecesariaParaAprobar = null;

        // 3. NUEVA REGLA: Si el alumno no tiene al menos 3 notas, la proyección no es realista.
        if (cantidadNotasRegistradas < 3) {
            estado = "INFO";
            mensaje = "Para una proyección precisa, necesitas registrar al menos 3 notas de la asignatura '" + asignatura.getNombre() + "'. ¡Sigue esforzándote!";
            return ResumenAcademicoResponse.builder()
                    .nombreAsignatura(asignatura.getNombre())
                    .promedioActual(Math.round(promedioActualAcumulado * 100.0) / 100.0)
                    .porcentajeRestante(porcentajeRestanteStr)
                    .estado(estado)
                    .mensajeInformativo(mensaje)
                    .build();
        }

        // 4. La Matemática (Solo se ejecuta si tiene 3 o más notas)
        // Ecuación: PromedioActual + (NotaNecesaria * PorcentajeRestante) = 12.5
        // Despeje: NotaNecesaria = (12.5 - PromedioActual) / PorcentajeRestante

        if (porcentajeRestanteDecimal > 0.001) { // Aún faltan notas por subir
            estado = "PENDIENTE";
            notaNecesariaParaAprobar = (12.5 - promedioActualAcumulado) / porcentajeRestanteDecimal;
            
            // Redondear a 2 decimales para limpieza
            notaNecesariaParaAprobar = Math.round(notaNecesariaParaAprobar * 100.0) / 100.0;

            if (notaNecesariaParaAprobar <= 0) {
                estado = "APROBADA (Proyectado)";
                mensaje = "¡Imparable! Con las notas que ya tienes, tienes tu curso aprobado asegurado aunque saques 0 en lo que falta.";
                notaNecesariaParaAprobar = 0.0;
            } else if (notaNecesariaParaAprobar > 20) {
                estado = "CRÍTICO";
                mensaje = "Lamentablemente, incluso sacando 20 en todo lo que falta, no alcanzarás el 12.5 mínimo para aprobar. Necesitas " + notaNecesariaParaAprobar + ".";
            } else {
                mensaje = "Necesitas sacar un promedio mínimo de " + notaNecesariaParaAprobar + " en el " + porcentajeRestanteStr + " restante de tu curso para alcanzar el 12.5 aprobatorio.";
            }
        } else {
            // El curso ya terminó al 100%
            if (promedioActualAcumulado >= 12.5) {
                estado = "APROBADA";
                mensaje = "¡Felicidades! Has terminado y aprobado el curso con éxito.";
            } else {
                estado = "DESAPROBADA";
                mensaje = "Has terminado el curso, pero no lograste la nota mínima aprobatoria.";
            }
        }

        return ResumenAcademicoResponse.builder()
                .nombreAsignatura(asignatura.getNombre())
                .promedioActual(Math.round(promedioActualAcumulado * 100.0) / 100.0)
                .porcentajeRestante(porcentajeRestanteStr)
                .estado(estado)
                .mensajeInformativo(mensaje)
                .build();
    }

    private Asignatura validarPropiedad(Integer asignaturaId) {
        String token = request.getHeader("Authorization");
        Integer usuarioId = jwtService.extractUserId(token.substring(7));

        // Validar usuario contra auth-service usando Feign
        try {
            usuarioClient.obtenerUsuario(usuarioId, token);
        } catch (Exception e) {
            throw new RuntimeException("Usuario no encontrado o error de comunicación con auth_service");
        }

        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        
        if (!asignatura.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para ver esta asignatura");
        }
        return asignatura;
    }
}
