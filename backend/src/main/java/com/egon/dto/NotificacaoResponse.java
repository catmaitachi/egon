package com.egon.dto;

import java.time.LocalDateTime;

import com.egon.model.Notificacao;

/** Um e-mail enviado pelo sistema, como aparece na caixa de avisos do painel. */
public record NotificacaoResponse(Integer id, String assunto, String descricao, LocalDateTime data, String codigoCupom) {

    public static NotificacaoResponse de(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getAssunto(),
                notificacao.getDescricao(),
                notificacao.getData(),
                notificacao.getCupom() == null ? null : notificacao.getCupom().getCodigo());
    }

}
