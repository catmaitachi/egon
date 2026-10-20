package com.egon.dto;

import com.egon.model.Aluno;

/** O aluno como a API devolve: tudo menos a senha. O saldo é só leitura: muda por transação e troca, não pelo cadastro. */
public record AlunoResponse(
        String cpf,
        String nome,
        String email,
        String rg,
        String endereco,
        InstituicaoResponse instituicao,
        CursoResponse curso,
        String login,
        int saldo) {

    public static AlunoResponse de(Aluno aluno) {
        return new AlunoResponse(
                aluno.getCpf(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getRg(),
                aluno.getEndereco(),
                InstituicaoResponse.de(aluno.getInstituicao()),
                CursoResponse.de(aluno.getCurso()),
                aluno.getLogin(),
                aluno.getSaldo());
    }

}
