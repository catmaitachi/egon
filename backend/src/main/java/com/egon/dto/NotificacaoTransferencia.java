package com.egon.dto;

/**
 * Aviso ao vivo de uma transferência entre alunos: {@code evento} é SOLICITADA, APROVADA,
 * RECUSADA ou CANCELADA, e {@code saldo} é o saldo de quem recebe o aviso.
 */
public record NotificacaoTransferencia(String tipo, String evento, TransferenciaResponse transferencia, int saldo) {

    public NotificacaoTransferencia(String evento, TransferenciaResponse transferencia, int saldo) {
        this("TRANSFERENCIA", evento, transferencia, saldo);
    }

}
