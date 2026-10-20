package com.egon.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela PROFESSOR. Professores não se cadastram: são pré-cadastrados com os dados que a
 * instituição fornece (HU04), e o saldo vem da cota semestral (HU05).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "professor")
public class Professor extends Usuario {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 80)
    private String departamento;

    /** Acumula de um semestre para o outro (HU05). */
    @Column(nullable = false)
    @ColumnDefault("0")
    private int saldo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cnpj_instituicao", nullable = false)
    private Instituicao instituicao;

    /** "Dá aula de": tabela PROFESSOR_CURSO. */
    @ManyToMany
    @JoinTable(name = "professor_curso",
            joinColumns = @JoinColumn(name = "cpf_professor"),
            inverseJoinColumns = @JoinColumn(name = "id_curso"))
    private Set<Curso> cursos = new LinkedHashSet<>();

    @Override
    public String getId() {
        return cpf;
    }

}
