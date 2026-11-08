package com.egon.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Tabela CUPOM (HU10): o código que o aluno mostra na loja e a empresa confere. Um por troca; o
 * código é aleatório ({@code EGON-XXXX-XXXX}) e é a chave.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "cupom")
public class Cupom {

    @Id
    @Column(length = 14)
    private String codigo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "troca_id", nullable = false, unique = true)
    private Troca troca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusCupom status;

    private Instant dataUso;

    public Cupom(String codigo, Troca troca) {
        this.codigo = codigo;
        this.troca = troca;
        this.status = StatusCupom.EMITIDO;
    }

    public void usar(Instant quando) {
        this.status = StatusCupom.USADO;
        this.dataUso = quando;
    }

}
