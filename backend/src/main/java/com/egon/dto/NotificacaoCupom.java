package com.egon.dto;

/** Aviso ao vivo de um cupom: EMITIDO (aluno e empresa) ou USADO (aluno). {@code saldo} é o de quem recebe. */
public record NotificacaoCupom(String tipo, String evento, CupomResponse cupom, int saldo) {

    public NotificacaoCupom(String evento, CupomResponse cupom, int saldo) {
        this("CUPOM", evento, cupom, saldo);
    }

}
