package com.egon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Administrador do Egon: cadastra instituições, cursos e professores (o pré-cadastro da HU04) e
 * gerencia alunos, empresas e outros admins. Não há cadastro público de admin.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "admin")
public class Admin extends Usuario {

    @Column(nullable = false, length = 100)
    private String nome;

}
