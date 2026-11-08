package com.egon.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
 * <p>O saldo não é coluna: cada Ego guarda o professor de origem, e o aluno tem uma
 * {@link Carteira} por professor. O saldo é a soma delas e começa em zero (HU02).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "aluno")
public class Aluno extends Usuario {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, unique = true, length = 11)
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
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

}
