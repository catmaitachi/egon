package com.egon.dto;

/**
 * Mensagem do WebSocket, do servidor para o cliente: {@code MOEDAS_RECEBIDAS} para o aluno,
 * {@code ENVIO_CONFIRMADO} para o professor, cada uma com o saldo novo de quem recebe. O texto
 * mostrado ao usuário é montado no front, que é onde mora o nome da moeda.
 */
public record Notificacao(String tipo, TransacaoResponse transacao, int saldo) {
}
