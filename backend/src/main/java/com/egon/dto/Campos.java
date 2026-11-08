package com.egon.dto;

import java.util.Locale;

/** Normalização dos campos de texto antes da validação: espaço sobrando não é erro de quem digitou. */
final class Campos {

    /**
     * Somado ao {@code @Email}, que aceita "ana@teste": o domínio precisa de um ponto, senão o
     * e-mail de contato do cadastro não chega a lugar nenhum.
     */
    static final String EMAIL_FORMATO = ".+@[^@]+\\.[^@.]+";

    private Campos() {
    }

    static String aparar(String valor) {
        return valor == null ? null : valor.strip();
    }

    /** E-mail e login são comparados por igualdade, então guardamos tudo em minúsculas. */
    static String minusculo(String valor) {
        return valor == null ? null : valor.strip().toLowerCase(Locale.ROOT);
    }

    /** Na edição, senha em branco quer dizer "manter a atual". */
    static String vazioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

}
