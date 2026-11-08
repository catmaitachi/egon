package com.egon.dto;

import java.util.UUID;

import com.egon.model.TipoVantagem;
import com.egon.model.Vantagem;

/** A vantagem como a empresa vê, com quantos alunos já têm saldo para ela (só a contagem). */
public record VantagemResponse(
        UUID id,
        String nome,
        String descricao,
        int custo,
        TipoVantagem tipo,
        String fotoUrl,
        long alunosComSaldo) {

    public static VantagemResponse de(Vantagem vantagem, long alunosComSaldo) {
        return new VantagemResponse(vantagem.getId(), vantagem.getNome(), vantagem.getDescricao(), vantagem.getCusto(),
                vantagem.getTipo(), fotoUrl(vantagem), alunosComSaldo);
    }

    /** Relativo à API: o front põe o prefixo dele na frente. */
    static String fotoUrl(Vantagem vantagem) {
        return "/vantagens/" + vantagem.getId() + "/foto";
    }

}
