package com.idat.pe.academic_service.repository;

import com.idat.pe.academic_service.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {
    
    // HU-05: Verifica si ya existe una nota para una ponderación específica
    boolean existsByPonderacionId(Integer ponderacionId);
    
    // HU-05: Busca evaluación por ponderación (para updates)
    Optional<Evaluacion> findByPonderacionId(Integer ponderacionId);
}
