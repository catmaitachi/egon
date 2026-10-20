package com.egon.dto;

import java.time.LocalDateTime;

/**
 * Uma linha de extrato (HU11, HU12). Os campos que não se aplicam à origem vêm nulos.
 *
 * @param tipo ENTRADA ou SAIDA
 * @param origem TRANSACAO (moedas de professor para aluno), TROCA (resgate de vantagem) ou
 *        COTA (crédito semestral do professor)
 * @param pessoa quem enviou (no extrato do aluno) ou quem recebeu (no do professor)
 */
public record LancamentoResponse(
        String tipo,
        String origem,
        LocalDateTime data,
        int valor,
        String pessoa,
        String motivo,
        String vantagem,
        String empresa,
        String codigoCupom,
        String semestre) {

    public static LancamentoResponse transacao(String tipo, LocalDateTime data, int valor, String pessoa, String motivo) {
        return new LancamentoResponse(tipo, "TRANSACAO", data, valor, pessoa, motivo, null, null, null, null);
    }

    public static LancamentoResponse troca(LocalDateTime data, int valor, String vantagem, String empresa, String codigoCupom) {
        return new LancamentoResponse("SAIDA", "TROCA", data, valor, null, null, vantagem, empresa, codigoCupom, null);
    }

    public static LancamentoResponse cota(LocalDateTime data, int valor, String semestre) {
        return new LancamentoResponse("ENTRADA", "COTA", data, valor, null, null, null, null, null, semestre);
    }

}
