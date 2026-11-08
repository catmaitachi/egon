package com.egon.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Repasse de Egos de um aluno a outro. Os Egos não mudam de origem: saem da carteira do remetente
 * com aquele professor e entram na do destinatário com o mesmo professor. Enquanto PENDENTE, o
 * valor já saiu do remetente (reservado), para não ser gasto duas vezes.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "transferencia")
public class Transferencia {

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
    @JoinColumn(name = "remetente_id", nullable = false)
    private Aluno remetente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Aluno destinatario;

    /** De quem são os Egos: as regras e a aprovação são deste professor. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTransferencia status;

    private Instant dataDecisao;

    public Transferencia(Instant data, int valor, String motivo, Aluno remetente, Aluno destinatario,
            Professor professor, StatusTransferencia status) {
        this.data = data;
        this.valor = valor;
        this.motivo = motivo;
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.professor = professor;
        this.status = status;
        if (status != StatusTransferencia.PENDENTE) {
            this.dataDecisao = data;
        }
    }

    public void decidir(StatusTransferencia decisao, Instant quando) {
        this.status = decisao;
        this.dataDecisao = quando;
    }

}
