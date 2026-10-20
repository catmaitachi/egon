package com.egon.dto;

import com.egon.model.Aluno;

/** O aluno na busca do professor (HU06): o suficiente para escolher o destinatário certo. */
public record AlunoResumoResponse(String cpf, String nome, String curso) {

    public static AlunoResumoResponse de(Aluno aluno) {
        return new AlunoResumoResponse(aluno.getCpf(), aluno.getNome(), aluno.getCurso().getNome());
    }

}
