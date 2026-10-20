package com.egon.model;

import java.time.LocalDateTime;

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
 * Crédito semestral de um professor (HU05), guardado para o histórico e a auditoria. A chave
 * única (professor, semestre) garante um crédito só por semestre, mesmo que a rotina rode de novo.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "cota_semestral", uniqueConstraints = @UniqueConstraint(columnNames = {"cpf_professor", "semestre"}))
public class CotaSemestral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cota")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpf_professor", nullable = false)
    private Professor professor;

    /** "2026.1" (janeiro a junho) ou "2026.2" (julho a dezembro). */
    @Column(nullable = false, length = 6)
    private String semestre;

    @Column(nullable = false)
    private int valor;

    @Column(nullable = false)
    private LocalDateTime data;

    public CotaSemestral(Professor professor, String semestre, int valor, LocalDateTime data) {
        this.professor = professor;
        this.semestre = semestre;
        this.valor = valor;
        this.data = data;
    }

}
