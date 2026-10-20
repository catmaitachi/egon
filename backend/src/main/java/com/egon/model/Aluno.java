package com.egon.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela ALUNO do modelo de dados (docs/Sprint 2/Modelo de Dados.md), com RG e endereço,
 * que a HU02 pede.
 *
 * <p>O saldo é uma coluna do aluno, como no MER: só existe uma moeda (Ego), então não
 * há carteira por tipo de moeda. A conta começa com zero (HU02).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "aluno")
public class Aluno extends Usuario {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String rg;

    @Column(nullable = false, length = 255)
    private String endereco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cnpj_instituicao", nullable = false)
    private Instituicao instituicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    /** O default no banco deixa o {@code ddl-auto=update} criar a coluna numa tabela que já tem alunos. */
    @Column(nullable = false)
    @ColumnDefault("0")
    private int saldo;

    @Override
    public String getId() {
        return cpf;
    }

}
