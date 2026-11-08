package com.egon.dto;

import java.time.Instant;

import com.egon.model.Cupom;
import com.egon.model.StatusCupom;
import com.egon.model.Troca;

/** O cupom como o aluno e a empresa veem: o código, o que foi resgatado, por quem e se já foi usado. */
public record CupomResponse(
        String codigo,
        StatusCupom status,
        Instant data,
        int valor,
        PessoaResponse vantagem,
        PessoaResponse empresa,
        PessoaResponse aluno,
        Instant dataUso) {

    public static CupomResponse de(Cupom cupom) {
        Troca troca = cupom.getTroca();
        return new CupomResponse(cupom.getCodigo(), cupom.getStatus(), troca.getData(), troca.getValor(),
                new PessoaResponse(troca.getVantagem().getId(), troca.getVantagem().getNome()),
                PessoaResponse.de(troca.getVantagem().getEmpresa()), PessoaResponse.de(troca.getAluno()),
                cupom.getDataUso());
    }

}
