package com.egon.dto;

import com.egon.model.Instituicao;

public record InstituicaoResponse(String cnpj, String nome, String tipo) {

    public static InstituicaoResponse de(Instituicao instituicao) {
        return new InstituicaoResponse(instituicao.getCnpj(), instituicao.getNome(), instituicao.getTipo());
    }

}
