package com.egon.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela EMPRESA do diagrama relacional, mais login e senha: a empresa entra no sistema
 * para cadastrar vantagens (HU01, HU03, HU08), e o diagrama não tinha onde guardá-los.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "empresa")
public class Empresa extends Usuario {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 14)
    private String cnpj;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Override
    public String getId() {
        return cnpj;
    }

}
