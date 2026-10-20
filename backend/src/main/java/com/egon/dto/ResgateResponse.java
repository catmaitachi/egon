package com.egon.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.egon.model.Aluno;
import com.egon.model.Troca;

/** Um resgate visto pela empresa, para conferir o cupom na troca presencial (HU10). */
public record ResgateResponse(
        UUID id,
        LocalDateTime data,
        int valor,
        String codigoCupom,
        String vantagem,
        AlunoResgate aluno) {

    public record AlunoResgate(String nome, String email, String curso, String instituicao) {
    }

    public static ResgateResponse de(Troca troca) {
        Aluno aluno = troca.getAluno();
        return new ResgateResponse(
                troca.getId(),
                troca.getData(),
                troca.getValor(),
                troca.getCupom() == null ? null : troca.getCupom().getCodigo(),
                troca.getVantagem().getNome(),
                new AlunoResgate(aluno.getNome(), aluno.getEmail(), aluno.getCurso().getNome(),
                        aluno.getInstituicao().getNome()));
    }

}
