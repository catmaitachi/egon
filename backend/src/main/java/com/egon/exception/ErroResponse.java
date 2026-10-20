package com.egon.exception;

import java.util.Map;

/**
 * Corpo de toda resposta de erro. {@code campos} liga cada campo inválido à sua mensagem
 * e vem vazio quando o erro não é de um campo específico.
 */
public record ErroResponse(String codigo, String mensagem, Map<String, String> campos) {

    public ErroResponse(String codigo, String mensagem) {
        this(codigo, mensagem, Map.of());
    }

}
