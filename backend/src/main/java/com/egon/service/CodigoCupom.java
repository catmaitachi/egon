package com.egon.service;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Código do cupom: {@code EGON-XXXX-XXXX}, com 8 caracteres do alfabeto de Crockford (sem I, L, O
 * e U, que se confundem) sorteados por {@link SecureRandom}: 40 bits, impossível de adivinhar, e
 * fácil de ditar na loja.
 */
public final class CodigoCupom {

    private static final String ALFABETO = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final SecureRandom SORTEIO = new SecureRandom();

    private CodigoCupom() {
    }

    public static String novo() {
        StringBuilder corpo = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            corpo.append(ALFABETO.charAt(SORTEIO.nextInt(ALFABETO.length())));
        }
        return "EGON-" + corpo.substring(0, 4) + "-" + corpo.substring(4);
    }

    /**
     * O que a pessoa digitou, no formato do código: sem caixa, espaços nem hífens, com ou sem o
     * prefixo, e as letras que parecem números trocadas por eles (O→0, I e L→1). Nulo se não der um
     * código possível.
     */
    public static String normalizar(String digitado) {
        if (digitado == null) {
            return null;
        }
        String limpo = digitado.toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
        if (limpo.startsWith("EGON")) {
            limpo = limpo.substring(4);
        }
        limpo = limpo.replace('O', '0').replace('I', '1').replace('L', '1');
        if (limpo.length() != 8 || !limpo.chars().allMatch(c -> ALFABETO.indexOf(c) >= 0)) {
            return null;
        }
        return "EGON-" + limpo.substring(0, 4) + "-" + limpo.substring(4);
    }

}
