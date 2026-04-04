package com.idat.pe.academic_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluacion",
        uniqueConstraints = {
    @UniqueConstraint(name = "uq_ponderacion_unica", columnNames = {"ponderacion_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ponderacion_id", nullable = false)
    private Ponderacion ponderacion;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer nota;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
