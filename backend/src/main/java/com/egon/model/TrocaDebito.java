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
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Quanto de uma troca saiu da carteira de cada professor: a origem dos Egos gastos fica registrada. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "troca_debito")
public class TrocaDebito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "troca_id", nullable = false)
    private Troca troca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Column(nullable = false)
    @Check(constraints = "valor > 0")
    private int valor;

    public TrocaDebito(Troca troca, Professor professor, int valor) {
        this.troca = troca;
        this.professor = professor;
        this.valor = valor;
    }

}
