package com.idat.pe.academic_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@FeignClient(name = "auth-service", url = "http://localhost:8081")
public interface UsuarioClient {

    @GetMapping("/api/usuarios/{usuarioId}")
    UsuarioDTO obtenerUsuario(
            @PathVariable("usuarioId") Integer usuarioId,
            @RequestHeader("Authorization") String token
    );

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class UsuarioDTO {
        private Integer id;
        private String nombre;
        private String email;
    }
}
