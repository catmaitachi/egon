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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Envio de moedas de um professor a um aluno, com a mensagem de reconhecimento (HU06). */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_transacao")
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false)
    private int valor;

    @Column(nullable = false, length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpf_professor", nullable = false)
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpf_aluno", nullable = false)
    private Aluno aluno;

    public Transacao(Professor professor, Aluno aluno, int valor, String motivo, LocalDateTime data) {
        this.professor = professor;
        this.aluno = aluno;
        this.valor = valor;
        this.motivo = motivo;
        this.data = data;
    }

}
