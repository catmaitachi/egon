package com.egon.service;

import com.egon.dto.TransacaoResponse;

/** Publicado pelo envio de moedas; quem escuta só age depois do commit. Leva os saldos já atualizados. */
public record TransacaoConfirmada(TransacaoResponse transacao, int saldoProfessor, int saldoAluno) {
}
