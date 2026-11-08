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
 * Tabela TROCA (HU09): o aluno resgatou uma vantagem e os Egos saíram do sistema. Nunca muda depois
 * de gravada; quanto saiu de cada carteira fica em {@link TrocaDebito}.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "troca")
public class Troca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Instant data;

    /** O custo da vantagem no momento do resgate. */
    @Column(nullable = false)
    @Check(constraints = "valor > 0")
    private int valor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vantagem_id", nullable = false)
    private Vantagem vantagem;

    public Troca(Instant data, int valor, Aluno aluno, Vantagem vantagem) {
        this.data = data;
        this.valor = valor;
        this.aluno = aluno;
        this.vantagem = vantagem;
    }

}
