package com.egon.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Instituição conveniada. Só é lida pela API: a lista vem pronta das instituições (HU04). */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "instituicao")
public class Instituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Setter
    @Column(nullable = false, length = 100)
    private String nome;

    @Setter
    @Column(nullable = false, length = 30)
    private String tipo;

    public Instituicao(String cnpj, String nome, String tipo) {
        this.cnpj = cnpj;
        this.nome = nome;
        this.tipo = tipo;
    }

}
