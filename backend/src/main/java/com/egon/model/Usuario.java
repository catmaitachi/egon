package com.egon.model;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * O que todo perfil que faz login tem em comum: login e senha.
 *
 * <p>O modelo de dados não tem tabela USUARIO: login e senha são colunas de cada
 * tabela de perfil (ALUNO, EMPRESA, PROFESSOR). Por isso aqui é {@code @MappedSuperclass}
 * e não uma entidade. A unicidade do login entre perfis, que o banco não garante, é
 * conferida pelo {@code UsuarioRepository}.
 *
 * <p>A chave de cada perfil é um documento atribuído por quem se cadastra (CPF, CNPJ), e
 * não um id gerado. Sem o {@link Persistable}, o {@code save} trataria todo cadastro como
 * possível edição e faria {@code merge}: dois cadastros simultâneos com o mesmo CPF
 * terminariam com o segundo sobrescrevendo o primeiro, em vez de esbarrar na chave primária.
 */
@MappedSuperclass
public abstract class Usuario implements Persistable<String> {

    /** Minúsculo para "Maria" e "maria" não virarem duas contas. */
    public static final String LOGIN_FORMATO = "[a-z0-9._-]{3,50}";

    @Getter
    @Setter
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    /** Hash BCrypt. A senha em texto nunca é gravada nem devolvida pela API. */
    @Getter
    @Setter
    @Column(nullable = false, length = 255)
    private String senha;

    @Transient
    private boolean novo = true;

    @Override
    public boolean isNew() {
        return novo;
    }

    /**
     * {@code @PrePersist}, e não {@code @PostPersist}: com chave atribuída o INSERT só sai no
     * flush, e até lá o {@code delete} do Spring Data ignoraria a entidade por achá-la nova.
     */
    @PrePersist
    @PostLoad
    void marcarComoPersistido() {
        novo = false;
    }

}
