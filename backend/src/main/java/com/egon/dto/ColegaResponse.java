package com.egon.dto;

import java.util.UUID;

import com.egon.model.Aluno;

/** Para escolher a quem enviar ou repassar: sem documento, contato nem endereço. */
public record ColegaResponse(UUID id, String nome, String curso) {

    public static ColegaResponse de(Aluno aluno) {
        return new ColegaResponse(aluno.getId(), aluno.getNome(), aluno.getCurso().getNome());
    }

}
