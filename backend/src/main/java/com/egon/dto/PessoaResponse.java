package com.egon.dto;

import java.util.UUID;

import com.egon.model.Aluno;
import com.egon.model.Empresa;
import com.egon.model.Professor;

/** A outra pessoa de um movimento: só o id e o nome, sem documento. */
public record PessoaResponse(UUID id, String nome) {

    public static PessoaResponse de(Professor professor) {
        return new PessoaResponse(professor.getId(), professor.getNome());
    }

    public static PessoaResponse de(Empresa empresa) {
        return new PessoaResponse(empresa.getId(), empresa.getNome());
    }

    public static PessoaResponse de(Aluno aluno) {
        return new PessoaResponse(aluno.getId(), aluno.getNome());
    }

}
