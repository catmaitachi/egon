package com.egon.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela PROFESSOR do modelo de dados. Cadastrado pelo admin a partir da lista da instituição (HU04).
 *
 * <p>Não tem e-mail: o aviso de envio confirmado é no app, ao vivo (HU07).
 * O saldo é o que o professor ainda pode enviar: vem da cota semestral (HU05) e sai nas transações.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "professor")
public class Professor extends Usuario {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String departamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int saldo;

    /** Regras de repasse dos Egos deste professor entre alunos. */
    @Embedded
    private RegrasMoeda regras = new RegrasMoeda();

}
