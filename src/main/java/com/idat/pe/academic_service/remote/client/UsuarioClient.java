package com.idat.pe.academic_service.remote.client;

import com.idat.pe.academic_service.remote.data.UsuarioDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface UsuarioClient {

    @GetMapping("/api/usuarios/{usuarioId}")
    UsuarioDto obtenerUsuario(
            @PathVariable("usuarioId") Integer usuarioId,
            @RequestHeader("Authorization") String token
    );
}
