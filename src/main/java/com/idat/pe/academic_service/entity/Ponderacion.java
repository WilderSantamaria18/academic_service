package com.idat.pe.academic_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ponderacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ponderacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal porcentaje;

    @Column(nullable = false)
    private Integer orden;

    @OneToOne(mappedBy = "ponderacion", fetch = FetchType.LAZY)
    private Evaluacion evaluacion;
}
