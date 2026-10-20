package com.egon.dto;

import java.util.Comparator;
import java.util.List;

import com.egon.model.Professor;

/** O professor como a API devolve: tudo menos a senha. */
public record ProfessorResponse(
        String cpf,
        String nome,
        String departamento,
        InstituicaoResponse instituicao,
        List<CursoResponse> cursos,
        String login,
        int saldo) {

    public static ProfessorResponse de(Professor professor) {
        return new ProfessorResponse(
                professor.getCpf(),
                professor.getNome(),
                professor.getDepartamento(),
                InstituicaoResponse.de(professor.getInstituicao()),
                professor.getCursos().stream().map(CursoResponse::de)
                        .sorted(Comparator.comparing(CursoResponse::nome)).toList(),
                professor.getLogin(),
                professor.getSaldo());
    }

}
