package com.egon.dto;

import java.util.UUID;

import com.egon.model.Instituicao;

public record InstituicaoResponse(UUID id, String cnpj, String nome, String tipo) {

    public static InstituicaoResponse de(Instituicao instituicao) {
        return new InstituicaoResponse(instituicao.getId(), instituicao.getCnpj(), instituicao.getNome(), instituicao.getTipo());
    }

}
