package com.egon.model;

import java.util.UUID;

import org.hibernate.annotations.Check;

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
 * Os Egos que um aluno tem de um professor. Cada Ego guarda o professor de origem, e as
 * regras de repasse são as desse professor; o saldo do aluno é a soma das carteiras dele.
 * Nasce no primeiro crédito.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "carteira", uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "professor_id"}))
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Column(nullable = false)
    @Check(constraints = "saldo >= 0")
    private int saldo;

    public Carteira(Aluno aluno, Professor professor) {
        this.aluno = aluno;
        this.professor = professor;
    }

    public void creditar(int valor) {
        saldo += valor;
    }

    public void debitar(int valor) {
        saldo -= valor;
    }

}
