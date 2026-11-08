package com.egon.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * O que todo perfil que faz login tem em comum: um id próprio, login e senha.
 *
 * <p>O modelo de dados não tem tabela USUARIO: login e senha são colunas de cada tabela de
 * perfil (ALUNO, PROFESSOR, EMPRESA, ADMIN). Por isso aqui é {@code @MappedSuperclass}. A
 * unicidade do login entre perfis, que o banco não garante, é conferida pelo
 * {@code UsuarioRepository}.
 *
 * <p>A chave é um UUID gerado, e não o CPF ou o CNPJ: o documento não aparece nas URLs nem se
 * espalha pelas chaves estrangeiras.
 */
@Getter
@MappedSuperclass
public abstract class Usuario {

    /** Minúsculo para "Maria" e "maria" não virarem duas contas. */
    public static final String LOGIN_FORMATO = "[a-z0-9._-]{3,50}";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    /** Hash BCrypt. A senha em texto nunca é gravada nem devolvida pela API. */
    @Setter
    @Column(nullable = false, length = 255)
    private String senha;

}
