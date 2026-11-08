package com.egon.dto;

import java.util.UUID;

import com.egon.model.Professor;

/** O professor como a API devolve: tudo menos a senha. */
public record ProfessorResponse(
        UUID id,
        String cpf,
        String nome,
        String departamento,
        InstituicaoResponse instituicao,
        String login,
        int saldo) {

    public static ProfessorResponse de(Professor professor) {
        return new ProfessorResponse(
                professor.getId(),
                professor.getCpf(),
                professor.getNome(),
                professor.getDepartamento(),
                InstituicaoResponse.de(professor.getInstituicao()),
                professor.getLogin(),
                professor.getSaldo());
    }

}
