package com.egon.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Crédito semestral de um professor (HU05). Fica no histórico para o extrato (HU12) e, por
 * ser único por professor e semestre, impede creditar duas vezes o mesmo semestre.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "cota_semestral", uniqueConstraints = @UniqueConstraint(columnNames = {"professor_id", "semestre"}))
public class CotaSemestral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    /** "2026.1" (janeiro a junho) ou "2026.2" (julho a dezembro). */
    @Column(nullable = false, length = 6)
    private String semestre;

    @Column(nullable = false)
    private int valor;

    @Column(nullable = false)
    private Instant data;

    public CotaSemestral(Professor professor, String semestre, int valor, Instant data) {
        this.professor = professor;
        this.semestre = semestre;
        this.valor = valor;
        this.data = data;
    }

}
