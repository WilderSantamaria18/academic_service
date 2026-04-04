package com.idat.pe.academic_service.repository;

import com.idat.pe.academic_service.entity.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsignaturaRepository extends JpaRepository<Asignatura, Integer> {
    List<Asignatura> findByUsuarioId(Integer usuarioId);
}
