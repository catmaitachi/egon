package com.egon.dto;

import java.util.Locale;

/** Normalização dos campos de texto antes da validação: espaço sobrando não é erro de quem digitou. */
final class Campos {

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
