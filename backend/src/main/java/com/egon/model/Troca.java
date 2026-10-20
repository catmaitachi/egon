package com.egon.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Resgate de uma vantagem por um aluno (HU09). {@code valor} é o custo debitado na hora. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "troca")
public class Troca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_troca")
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false)
    private int valor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpf_aluno", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vantagem", nullable = false)
    private Vantagem vantagem;

    /** Lado inverso: a chave estrangeira fica em CUPOM. Quem cria o cupom preenche este lado também. */
    @Setter
    @OneToOne(mappedBy = "troca", fetch = FetchType.LAZY)
    private Cupom cupom;

    public Troca(Aluno aluno, Vantagem vantagem, LocalDateTime data) {
        this.aluno = aluno;
        this.vantagem = vantagem;
        this.valor = vantagem.getCusto();
        this.data = data;
    }

}
