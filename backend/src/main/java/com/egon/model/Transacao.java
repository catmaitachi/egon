package com.egon.model;

import java.time.Instant;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Tabela TRANSACAO: moedas que um professor enviou a um aluno (HU06). Nunca muda depois de
 * gravada; é o histórico dos dois extratos (HU11, HU12).
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Instant data;

    @Column(nullable = false)
    @Check(constraints = "valor > 0")
    private int valor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    public Transacao(Instant data, int valor, String motivo, Professor professor, Aluno aluno) {
        this.data = data;
        this.valor = valor;
        this.motivo = motivo;
        this.professor = professor;
        this.aluno = aluno;
    }

}
