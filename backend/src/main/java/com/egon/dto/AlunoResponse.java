package com.egon.dto;

import java.util.UUID;

import com.egon.model.Aluno;

/** O aluno como a API devolve: tudo menos a senha. O saldo é só leitura: muda por transação e troca, não pelo cadastro. */
public record AlunoResponse(
        UUID id,
        String cpf,
        String nome,
        String email,
        String rg,
        String endereco,
        InstituicaoResponse instituicao,
        CursoResponse curso,
        String login,
        int saldo) {

    /** O saldo vem de fora: é a soma das carteiras do aluno. */
    public static AlunoResponse de(Aluno aluno, int saldo) {
        return new AlunoResponse(
                aluno.getId(),
                aluno.getCpf(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getRg(),
                aluno.getEndereco(),
                InstituicaoResponse.de(aluno.getInstituicao()),
                CursoResponse.de(aluno.getCurso()),
                aluno.getLogin(),
                saldo);
    }

}
