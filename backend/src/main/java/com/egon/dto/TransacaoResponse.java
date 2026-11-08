package com.egon.dto;

import java.time.Instant;
import java.util.UUID;

import com.egon.model.Transacao;

public record TransacaoResponse(
        UUID id,
        Instant data,
        int valor,
        String motivo,
        PessoaResponse professor,
        PessoaResponse aluno) {

    public static TransacaoResponse de(Transacao transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getData(),
                transacao.getValor(),
                transacao.getMotivo(),
                PessoaResponse.de(transacao.getProfessor()),
                PessoaResponse.de(transacao.getAluno()));
    }

}
