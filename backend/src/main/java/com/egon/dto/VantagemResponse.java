package com.egon.dto;

import com.egon.model.TipoVantagem;
import com.egon.model.Vantagem;

/** Card do catálogo (HU09): foto, descrição, empresa e custo. */
public record VantagemResponse(
        Integer id,
        String nome,
        String descricao,
        int custo,
        TipoVantagem tipo,
        String fotoUrl,
        EmpresaResumo empresa) {

    public record EmpresaResumo(String cnpj, String nome) {
    }

    public static VantagemResponse de(Vantagem vantagem) {
        return new VantagemResponse(
                vantagem.getId(),
                vantagem.getNome(),
                vantagem.getDescricao(),
                vantagem.getCusto(),
                vantagem.getTipo(),
                "/vantagens/" + vantagem.getId() + "/foto",
                new EmpresaResumo(vantagem.getEmpresa().getCnpj(), vantagem.getEmpresa().getNome()));
    }

}
