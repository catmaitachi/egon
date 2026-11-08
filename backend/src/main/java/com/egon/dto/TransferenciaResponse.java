package com.egon.dto;

import java.time.Instant;
import java.util.UUID;

import com.egon.model.StatusTransferencia;
import com.egon.model.Transferencia;

public record TransferenciaResponse(
        UUID id,
        Instant data,
        int valor,
        String motivo,
        StatusTransferencia status,
        PessoaResponse remetente,
        PessoaResponse destinatario,
        PessoaResponse professor,
        Instant dataDecisao) {

    public static TransferenciaResponse de(Transferencia t) {
        return new TransferenciaResponse(t.getId(), t.getData(), t.getValor(), t.getMotivo(), t.getStatus(),
                PessoaResponse.de(t.getRemetente()), PessoaResponse.de(t.getDestinatario()),
                PessoaResponse.de(t.getProfessor()), t.getDataDecisao());
    }

}
