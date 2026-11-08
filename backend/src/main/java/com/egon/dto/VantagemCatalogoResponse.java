package com.egon.dto;

import java.util.UUID;

import com.egon.model.TipoVantagem;
import com.egon.model.Vantagem;

/** Um item do catálogo dos alunos (HU09): a vantagem e a empresa que oferece. */
public record VantagemCatalogoResponse(
        UUID id,
        String nome,
        String descricao,
        int custo,
        TipoVantagem tipo,
        String fotoUrl,
        PessoaResponse empresa) {

    public static VantagemCatalogoResponse de(Vantagem vantagem) {
        return new VantagemCatalogoResponse(vantagem.getId(), vantagem.getNome(), vantagem.getDescricao(),
                vantagem.getCusto(), vantagem.getTipo(), VantagemResponse.fotoUrl(vantagem),
                PessoaResponse.de(vantagem.getEmpresa()));
    }

}
